-- =====================================================================
-- Mary Hair Salon — database schema (Supabase / PostgreSQL)
-- Owner: Luthando Didiza (Database/Backend)
--
-- How to run: Supabase dashboard -> SQL Editor -> New query -> paste this
-- whole file -> Run. Then run 002_security.sql and 003_api.sql the same way.
--
-- Tables
--   profiles    one row per account (customer / employee / manager)
--   categories  service categories (Braids, Locs, ...)
--   services    bookable services with price and discount
--   bookings    appointments (customer -> stylist -> service)
--   messages    website contact-form messages
-- =====================================================================

-- ---------- enum types (restrict columns to valid values) ----------
create type public.user_role as enum ('customer', 'employee', 'manager');
create type public.booking_status as enum ('pending', 'upcoming', 'completed', 'cancelled');
create type public.booking_type as enum ('Incall', 'Outcall');

-- ---------- helper: keep updated_at current ----------
create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at := now();
  return new;
end;
$$;

-- ---------- profiles ----------
-- 1:1 with Supabase Auth users. Passwords live in auth.users, hashed with bcrypt
-- by Supabase; this table never stores a password.
create table public.profiles (
  id                     uuid primary key references auth.users (id) on delete cascade,
  name                   text not null default '',
  surname                text not null default '',
  email                  text not null unique,
  contact                text not null default '',
  role                   public.user_role not null default 'customer',
  title                  text not null default '',          -- stylist job title
  photo_url              text not null default '',
  notifications_enabled  boolean not null default true,
  created_at             timestamptz not null default now(),
  updated_at             timestamptz not null default now()
);
create index profiles_role_idx on public.profiles (role);
create trigger profiles_updated_at before update on public.profiles
  for each row execute function public.set_updated_at();

-- ---------- categories ----------
create table public.categories (
  id          text primary key check (id ~ '^[a-z0-9-]+$'),   -- readable slug, e.g. 'braids'
  label       text not null check (length(trim(label)) > 0),
  description text not null default '',
  image       text not null default '',
  icon        text not null default '',
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now()
);
create trigger categories_updated_at before update on public.categories
  for each row execute function public.set_updated_at();

-- ---------- services ----------
create table public.services (
  id           text primary key check (id ~ '^[a-z0-9-]+$'),
  category_id  text not null references public.categories (id) on update cascade on delete restrict,
  title        text not null check (length(trim(title)) > 0),
  price        numeric(10, 2) not null check (price > 0),           -- Rand
  description  text not null default '',
  tags         text[] not null default '{}',
  image        text not null default '',
  discount     numeric(5, 2) not null default 0 check (discount between 0 and 100),  -- percent
  is_active    boolean not null default true,
  created_at   timestamptz not null default now(),
  updated_at   timestamptz not null default now()
);
create index services_category_idx on public.services (category_id);
create trigger services_updated_at before update on public.services
  for each row execute function public.set_updated_at();

-- ---------- bookings ----------
create table public.bookings (
  id                 uuid primary key default gen_random_uuid(),
  customer_id        uuid not null references public.profiles (id) on delete cascade,
  stylist_id         uuid not null references public.profiles (id) on delete restrict,
  service_id         text not null references public.services (id) on delete restrict,
  customer_name      text not null check (length(trim(customer_name)) > 0),
  customer_surname   text not null check (length(trim(customer_surname)) > 0),
  contact            text not null,
  type               public.booking_type not null default 'Incall',
  location           text not null default '',
  booking_date       date not null,
  booking_time       time not null,
  notes              text not null default '' check (length(notes) <= 500),
  -- Price is copied from the service at booking time, so later price changes
  -- don't alter existing bookings.
  cost               numeric(10, 2) not null check (cost >= 0),
  discount           numeric(5, 2) not null default 0 check (discount between 0 and 100),
  total              numeric(10, 2) not null check (total >= 0),
  status             public.booking_status not null default 'pending',
  employee_notified  boolean not null default false,
  customer_notified  boolean not null default true,
  created_at         timestamptz not null default now(),
  updated_at         timestamptz not null default now(),

  constraint bookings_outcall_needs_location
    check (type <> 'Outcall' or length(trim(location)) > 0),
  constraint bookings_time_slot
    check (booking_time between time '08:00' and time '19:00' and extract(minute from booking_time) = 0),
  constraint bookings_customer_not_stylist
    check (customer_id <> stylist_id)
);
create index bookings_customer_idx on public.bookings (customer_id);
create index bookings_stylist_idx on public.bookings (stylist_id, booking_date);
create trigger bookings_updated_at before update on public.bookings
  for each row execute function public.set_updated_at();

-- Double-booking protection (spec FR-05 / NFR-04), enforced by the database itself:
-- a stylist can have only one ACTIVE booking per date + time. Cancelled and completed
-- bookings don't count, so a cancelled slot can be booked again.
create unique index bookings_no_double_booking
  on public.bookings (stylist_id, booking_date, booking_time)
  where status in ('pending', 'upcoming');

-- ---------- messages (website contact form) ----------
create table public.messages (
  id          uuid primary key default gen_random_uuid(),
  name        text not null check (length(trim(name)) > 0),
  email       text not null check (email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$'),
  message     text not null check (length(trim(message)) between 1 and 2000),
  created_at  timestamptz not null default now()
);

-- ---------- view the apps read bookings through ----------
-- Joins in the service, category and stylist names so the app needs one request.
-- security_invoker = true makes the view obey the caller's row-level security.
create view public.booking_details
with (security_invoker = true) as
select
  b.id,
  b.customer_id,
  b.stylist_id,
  trim(s.name || ' ' || s.surname) as stylist_name,
  b.service_id,
  sv.title       as service_title,
  sv.category_id,
  c.label        as category_label,
  b.customer_name,
  b.customer_surname,
  b.contact,
  b.type,
  b.location,
  b.booking_date,
  to_char(b.booking_time, 'HH24:MI') as booking_time,
  b.notes,
  b.cost,
  b.discount,
  b.total,
  b.status,
  b.created_at,
  b.updated_at
from public.bookings b
join public.services   sv on sv.id = b.service_id
join public.categories c  on c.id  = sv.category_id
left join public.profiles s on s.id = b.stylist_id;

-- ---------- create a profile automatically for every new account ----------
-- Works for email sign-up (name/surname/contact passed as metadata) and for
-- Google SSO (full_name / avatar_url supplied by Google).
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  meta jsonb := coalesce(new.raw_user_meta_data, '{}'::jsonb);
  full_name text := coalesce(meta ->> 'full_name', meta ->> 'name', '');
begin
  -- Email sign-up sends first_name + surname; Google sends full_name ("Ayanda Zulu").
  insert into public.profiles (id, email, name, surname, contact, photo_url)
  values (
    new.id,
    coalesce(new.email, ''),
    coalesce(nullif(meta ->> 'first_name', ''), split_part(full_name, ' ', 1)),
    coalesce(nullif(meta ->> 'surname', ''), trim(substr(full_name, length(split_part(full_name, ' ', 1)) + 1))),
    coalesce(meta ->> 'contact', ''),
    coalesce(meta ->> 'avatar_url', meta ->> 'picture', '')
  )
  on conflict (id) do nothing;
  return new;
end;
$$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();
