-- =====================================================================
-- Mary Hair Salon — public stylist list (website home + About pages)
-- Run after 003_api.sql.
--
-- The profiles table is private (it holds emails and phone numbers), so
-- logged-out visitors can't read it. This function returns ONLY the
-- public details of stylists (name, title, photo) so the "Meet the Team"
-- sections can come from the database for everyone.
-- =====================================================================

create or replace function public.public_stylists()
returns table (
  id uuid,
  name text,
  surname text,
  title text,
  photo_url text
)
language sql
stable
security definer
set search_path = public
as $$
  select p.id, p.name, p.surname, p.title, p.photo_url
    from public.profiles p
   where p.role = 'employee'
   order by p.name, p.surname;
$$;

revoke execute on function public.public_stylists() from public;
grant execute on function public.public_stylists() to anon, authenticated;
