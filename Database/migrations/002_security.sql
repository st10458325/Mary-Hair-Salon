-- =====================================================================
-- Mary Hair Salon — Row Level Security (spec NFR-03)
-- Run after 001_schema.sql.
--
-- Supabase exposes every table through its REST API, so these policies are
-- what stop a user reading or changing data that isn't theirs.
--   anon          = not logged in
--   authenticated = any logged-in user (role checked with my_role())
-- Bookings can't be inserted/updated directly at all: the app must go through
-- the API functions in 003_api.sql, which apply the business rules.
-- =====================================================================

-- Role of the logged-in user. SECURITY DEFINER so it can read profiles without
-- triggering the profiles policies (which would otherwise call it recursively).
create or replace function public.my_role()
returns public.user_role
language sql
stable
security definer
set search_path = public
as $$
  select role from public.profiles where id = auth.uid();
$$;

alter table public.profiles   enable row level security;
alter table public.categories enable row level security;
alter table public.services   enable row level security;
alter table public.bookings   enable row level security;
alter table public.messages   enable row level security;

-- ---------- profiles ----------
-- You can read your own profile and every stylist's profile (booking form / About
-- screen). Managers can read everyone (Staff tab).
create policy profiles_select on public.profiles
  for select to authenticated
  using (id = auth.uid() or role = 'employee' or public.my_role() = 'manager');

-- Settings screen: you may update only your own row...
create policy profiles_update_own on public.profiles
  for update to authenticated
  using (id = auth.uid())
  with check (id = auth.uid());

-- ...and only these columns. role and email can never be changed by the user
-- (roles change through set_user_role() in 003_api.sql, managers only).
revoke insert, update, delete on public.profiles from anon, authenticated;
grant update (name, surname, contact, photo_url, notifications_enabled) on public.profiles to authenticated;

-- ---------- categories & services: public catalogue, manager-only edits ----------
create policy categories_read on public.categories
  for select to anon, authenticated using (true);
create policy categories_manager_insert on public.categories
  for insert to authenticated with check (public.my_role() = 'manager');
create policy categories_manager_update on public.categories
  for update to authenticated using (public.my_role() = 'manager') with check (public.my_role() = 'manager');
create policy categories_manager_delete on public.categories
  for delete to authenticated using (public.my_role() = 'manager');

create policy services_read on public.services
  for select to anon, authenticated using (true);
create policy services_manager_insert on public.services
  for insert to authenticated with check (public.my_role() = 'manager');
create policy services_manager_update on public.services
  for update to authenticated using (public.my_role() = 'manager') with check (public.my_role() = 'manager');
create policy services_manager_delete on public.services
  for delete to authenticated using (public.my_role() = 'manager');

-- ---------- bookings ----------
-- Customers see their own bookings, stylists see bookings assigned to them,
-- managers see everything. No insert/update policies on purpose: writes go
-- through the API functions only.
create policy bookings_select on public.bookings
  for select to authenticated
  using (customer_id = auth.uid() or stylist_id = auth.uid() or public.my_role() = 'manager');

create policy bookings_manager_delete on public.bookings
  for delete to authenticated using (public.my_role() = 'manager');

revoke insert, update on public.bookings from anon, authenticated;

-- ---------- messages (website contact form) ----------
create policy messages_insert on public.messages
  for insert to anon, authenticated with check (true);
create policy messages_manager_read on public.messages
  for select to authenticated using (public.my_role() = 'manager');
create policy messages_manager_delete on public.messages
  for delete to authenticated using (public.my_role() = 'manager');

revoke update on public.messages from anon, authenticated;
