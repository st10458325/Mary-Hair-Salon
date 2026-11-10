-- =====================================================================
-- Mary Hair Salon — API functions (called from the app with supabase.postgrest.rpc)
-- Run after 002_security.sql.
--
-- These hold the business rules that must not be trusted to the phone:
-- the price is read from the services table, time slots are validated,
-- double-booking is caught, and each role can only make allowed changes.
--
-- Error messages that start with "USER:" are meant to be shown to the user
-- as-is; the app strips the prefix.
-- =====================================================================

-- ---------- shared validation ----------
create or replace function public._validate_booking_input(
  p_type public.booking_type,
  p_location text,
  p_date date,
  p_time time,
  p_name text,
  p_surname text,
  p_contact text,
  p_notes text
) returns void
language plpgsql
stable
set search_path = public
as $$
declare
  today date := (now() at time zone 'Africa/Johannesburg')::date;
  now_time time := (now() at time zone 'Africa/Johannesburg')::time;
begin
  if coalesce(trim(p_name), '') = '' or coalesce(trim(p_surname), '') = '' then
    raise exception 'USER: Please enter your name and surname.';
  end if;
  if regexp_replace(coalesce(p_contact, ''), '\s', '', 'g') !~ '^(0[0-9]{9}|\+27[0-9]{9})$' then
    raise exception 'USER: Please enter a valid cell number, e.g. 0821234567.';
  end if;
  if p_date is null or p_date < today then
    raise exception 'USER: You can''t book a date in the past.';
  end if;
  if p_time is null or p_time < time '08:00' or p_time > time '19:00' or extract(minute from p_time) <> 0 then
    raise exception 'USER: Please choose a time slot between 08:00 and 19:00.';
  end if;
  if p_date = today and p_time <= now_time then
    raise exception 'USER: That time has already passed today.';
  end if;
  if p_type = 'Outcall' and coalesce(trim(p_location), '') = '' then
    raise exception 'USER: Please enter an address for an outcall booking.';
  end if;
  if length(coalesce(p_notes, '')) > 500 then
    raise exception 'USER: Notes must be 500 characters or fewer.';
  end if;
end;
$$;

create or replace function public._require_stylist(p_stylist_id uuid)
returns void
language plpgsql
stable
security definer
set search_path = public
as $$
begin
  if not exists (select 1 from public.profiles where id = p_stylist_id and role = 'employee') then
    raise exception 'USER: That stylist is no longer available.';
  end if;
end;
$$;

-- ---------- create_booking (customer) ----------
create or replace function public.create_booking(
  p_service_id text,
  p_stylist_id uuid,
  p_type public.booking_type,
  p_location text,
  p_date date,
  p_time time,
  p_notes text,
  p_name text,
  p_surname text,
  p_contact text
) returns uuid
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid uuid := auth.uid();
  v_service public.services%rowtype;
  v_id uuid;
begin
  if v_uid is null then
    raise exception 'USER: Please log in to book an appointment.';
  end if;
  perform public._validate_booking_input(p_type, p_location, p_date, p_time, p_name, p_surname, p_contact, p_notes);
  perform public._require_stylist(p_stylist_id);
  if p_stylist_id = v_uid then
    raise exception 'USER: You can''t book yourself.';
  end if;

  select * into v_service from public.services where id = p_service_id and is_active;
  if not found then
    raise exception 'USER: That service is no longer available.';
  end if;

  begin
    insert into public.bookings (
      customer_id, stylist_id, service_id, customer_name, customer_surname, contact,
      type, location, booking_date, booking_time, notes,
      cost, discount, total, status, employee_notified, customer_notified
    ) values (
      v_uid, p_stylist_id, p_service_id, trim(p_name), trim(p_surname),
      regexp_replace(p_contact, '\s', '', 'g'),
      p_type, case when p_type = 'Outcall' then trim(p_location) else '' end,
      p_date, p_time, trim(coalesce(p_notes, '')),
      v_service.price, v_service.discount,
      round(v_service.price * (1 - v_service.discount / 100), 2),
      'pending', false, true
    ) returning id into v_id;
  exception when unique_violation then
    raise exception 'USER: Sorry, that time slot has just been taken. Please pick another time.';
  end;

  return v_id;
end;
$$;

-- ---------- reschedule_booking (customer) ----------
create or replace function public.reschedule_booking(
  p_booking_id uuid,
  p_service_id text,
  p_stylist_id uuid,
  p_type public.booking_type,
  p_location text,
  p_date date,
  p_time time,
  p_notes text,
  p_name text,
  p_surname text,
  p_contact text
) returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid uuid := auth.uid();
  v_booking public.bookings%rowtype;
  v_service public.services%rowtype;
begin
  select * into v_booking from public.bookings where id = p_booking_id for update;
  if not found or v_booking.customer_id is distinct from v_uid then
    raise exception 'USER: You can only change your own bookings.';
  end if;
  if v_booking.status not in ('pending', 'upcoming') then
    raise exception 'USER: Only pending or upcoming bookings can be rescheduled.';
  end if;
  perform public._validate_booking_input(p_type, p_location, p_date, p_time, p_name, p_surname, p_contact, p_notes);
  perform public._require_stylist(p_stylist_id);

  select * into v_service from public.services where id = p_service_id and is_active;
  if not found then
    raise exception 'USER: That service is no longer available.';
  end if;

  begin
    update public.bookings set
      stylist_id = p_stylist_id,
      service_id = p_service_id,
      customer_name = trim(p_name),
      customer_surname = trim(p_surname),
      contact = regexp_replace(p_contact, '\s', '', 'g'),
      type = p_type,
      location = case when p_type = 'Outcall' then trim(p_location) else '' end,
      booking_date = p_date,
      booking_time = p_time,
      notes = trim(coalesce(p_notes, '')),
      cost = v_service.price,
      discount = v_service.discount,
      total = round(v_service.price * (1 - v_service.discount / 100), 2),
      status = 'pending',                -- stylist must re-confirm
      employee_notified = false
    where id = p_booking_id;
  exception when unique_violation then
    raise exception 'USER: Sorry, that time slot has just been taken. Please pick another time.';
  end;
end;
$$;

-- ---------- set_booking_status (customer cancel / stylist accept-decline-complete / manager) ----------
create or replace function public.set_booking_status(
  p_booking_id uuid,
  p_status public.booking_status
) returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  v_uid uuid := auth.uid();
  v_role public.user_role := public.my_role();
  v_booking public.bookings%rowtype;
  v_allowed boolean;
begin
  select * into v_booking from public.bookings where id = p_booking_id for update;
  if not found then
    raise exception 'USER: That booking no longer exists.';
  end if;

  if v_role = 'manager' then
    v_allowed := true;                                   -- spec FR-08: change status directly
  elsif v_role = 'employee' and v_booking.stylist_id = v_uid then
    v_allowed := (v_booking.status = 'pending'  and p_status in ('upcoming', 'cancelled'))
              or (v_booking.status = 'upcoming' and p_status in ('completed', 'cancelled'));
  elsif v_booking.customer_id = v_uid then
    v_allowed := v_booking.status in ('pending', 'upcoming') and p_status = 'cancelled';
  else
    raise exception 'USER: You don''t have permission to change this booking.';
  end if;

  if not v_allowed then
    raise exception 'USER: This booking is % and can''t be changed to %.', v_booking.status, p_status;
  end if;

  begin
    update public.bookings
       set status = p_status,
           customer_notified = (v_booking.customer_id = v_uid),
           employee_notified = (v_booking.stylist_id = v_uid)
     where id = p_booking_id;
  exception when unique_violation then
    -- e.g. a manager re-activating a cancelled booking whose slot is now taken
    raise exception 'USER: That time slot is already taken by another booking.';
  end;
end;
$$;

-- ---------- reassign_stylist (manager, spec FR-08) ----------
create or replace function public.reassign_stylist(
  p_booking_id uuid,
  p_stylist_id uuid
) returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  if public.my_role() is distinct from 'manager' then
    raise exception 'USER: Only a manager can reassign bookings.';
  end if;
  perform public._require_stylist(p_stylist_id);
  begin
    update public.bookings
       set stylist_id = p_stylist_id, employee_notified = false, customer_notified = false
     where id = p_booking_id;
  exception when unique_violation then
    raise exception 'USER: That stylist is already booked at this time.';
  end;
  if not found then
    raise exception 'USER: That booking no longer exists.';
  end if;
end;
$$;

-- ---------- set_user_role (manager, spec FR-09) ----------
create or replace function public.set_user_role(
  p_user_id uuid,
  p_role public.user_role,
  p_title text default ''
) returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  v_current public.user_role;
begin
  if public.my_role() is distinct from 'manager' then
    raise exception 'USER: Only a manager can change roles.';
  end if;
  if p_role = 'manager' then
    raise exception 'USER: Manager accounts can only be created in the Supabase dashboard.';
  end if;
  select role into v_current from public.profiles where id = p_user_id;
  if not found then
    raise exception 'USER: That user no longer exists.';
  end if;
  if v_current = 'manager' then
    raise exception 'USER: You can''t change a manager''s role.';
  end if;
  update public.profiles
     set role = p_role,
         title = case when p_role = 'employee' then coalesce(nullif(trim(p_title), ''), title) else '' end
   where id = p_user_id;
end;
$$;

-- ---------- taken_times (booking form greys out busy slots) ----------
-- Returns only times, never who booked them.
create or replace function public.taken_times(
  p_stylist_id uuid,
  p_date date
) returns table (booking_time text)
language sql
stable
security definer
set search_path = public
as $$
  select to_char(b.booking_time, 'HH24:MI')
    from public.bookings b
   where b.stylist_id = p_stylist_id
     and b.booking_date = p_date
     and b.status in ('pending', 'upcoming')
   order by 1;
$$;

-- ---------- who may call what ----------
-- Postgres lets everyone execute functions by default; lock that down.
revoke execute on function public._validate_booking_input(public.booking_type, text, date, time, text, text, text, text) from public, anon, authenticated;
revoke execute on function public._require_stylist(uuid) from public, anon, authenticated;
revoke execute on function public.handle_new_user() from public, anon, authenticated;

revoke execute on function public.create_booking(text, uuid, public.booking_type, text, date, time, text, text, text, text) from public, anon;
revoke execute on function public.reschedule_booking(uuid, text, uuid, public.booking_type, text, date, time, text, text, text, text) from public, anon;
revoke execute on function public.set_booking_status(uuid, public.booking_status) from public, anon;
revoke execute on function public.reassign_stylist(uuid, uuid) from public, anon;
revoke execute on function public.set_user_role(uuid, public.user_role, text) from public, anon;
revoke execute on function public.taken_times(uuid, date) from public, anon;

grant execute on function public.create_booking(text, uuid, public.booking_type, text, date, time, text, text, text, text) to authenticated;
grant execute on function public.reschedule_booking(uuid, text, uuid, public.booking_type, text, date, time, text, text, text, text) to authenticated;
grant execute on function public.set_booking_status(uuid, public.booking_status) to authenticated;
grant execute on function public.reassign_stylist(uuid, uuid) to authenticated;
grant execute on function public.set_user_role(uuid, public.user_role, text) to authenticated;
grant execute on function public.taken_times(uuid, date) to authenticated;
