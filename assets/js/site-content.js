/**
 * assets/js/site-content.js
 * ---------------------------------------------------------------
 * Fills the home and About page sections from the Supabase database
 * instead of hardcoded PHP arrays:
 *   #featuredStyles   -> the 3 highest-priced active services
 *   #dealsSlider      -> every service with a discount
 *   #teamSlider       -> stylists (public_stylists() database function)
 *   [data-stat="stylists"] -> live number of stylists
 * Include once per page: <script type="module" src="assets/js/site-content.js"></script>
 * ---------------------------------------------------------------
 */
import { supabase, formatZAR } from './supabase-client.js';

// Used when a service/stylist has no image yet in the database.
const FALLBACK_IMAGES = ['assets/images/kael.png', 'assets/images/FancyMan.png', 'assets/images/Mahoraga.png'];
const fallbackImage = i => FALLBACK_IMAGES[i % FALLBACK_IMAGES.length];

function escapeHtml(str){
  const div = document.createElement('div');
  div.textContent = str ?? '';
  return div.innerHTML;
}

function sliderCard({ image, badge, badgeVariant, title, href }){
  return `
    <a href="${escapeHtml(href)}" class="pic-slider__card glass">
      <img src="${escapeHtml(image)}" alt="${escapeHtml(title)}" loading="lazy">
      ${badge ? `<span class="pic-slider__badge${badgeVariant ? ' pic-slider__badge--' + badgeVariant : ''}">${escapeHtml(badge)}</span>` : ''}
      <h3 class="pic-slider__card-title">${escapeHtml(title)}</h3>
    </a>`;
}

function fillSlider(slider, cardsHtml, emptyText){
  const track = slider.querySelector('.pic-slider__track');
  track.innerHTML = cardsHtml || `<p class="site-content-empty">${escapeHtml(emptyText)}</p>`;
  slider.dispatchEvent(new Event('slider:refresh'));
}

let servicesPromise;
function loadServices(){
  servicesPromise ||= supabase
    .from('services')
    .select('id, title, price, discount, image')
    .eq('is_active', true)
    .then(({ data, error }) => { if(error) throw error; return data; });
  return servicesPromise;
}

async function fillFeatured(section){
  const grid = section.querySelector('.featured-style__grid');
  try {
    const services = (await loadServices()).slice().sort((a, b) => b.price - a.price).slice(0, 3);
    grid.innerHTML = services.map((s, i) => `
      <a href="services.php" class="featured-style__card glass">
        <img src="${escapeHtml(s.image || fallbackImage(i))}" alt="${escapeHtml(s.title)}" loading="lazy">
        <div class="featured-style__overlay"><span>${escapeHtml(s.title)}</span></div>
      </a>`).join('') || '<p class="site-content-empty">No styles yet.</p>';
  } catch(err){
    console.error('Featured styles', err);
    grid.innerHTML = '<p class="site-content-empty">Could not load styles. Please refresh.</p>';
  }
}

async function fillDeals(slider){
  try {
    const deals = (await loadServices())
      .filter(s => Number(s.discount) > 0)
      .sort((a, b) => b.discount - a.discount);
    const html = deals.map((s, i) => {
      const now = Math.round(s.price * (1 - s.discount / 100) * 100) / 100;
      return sliderCard({
        image: s.image || fallbackImage(i),
        badge: `Save ${Number(s.discount)}%`,
        badgeVariant: 'deal',
        title: `${s.title} — was ${formatZAR(s.price)}, now ${formatZAR(now)}`,
        href: 'services.php',
      });
    }).join('');
    fillSlider(slider, html, 'No deals right now — check back soon.');
  } catch(err){
    console.error('Deals', err);
    fillSlider(slider, '', 'Could not load deals. Please refresh.');
  }
}

// Stylists without a photo get a coloured initials avatar (same style as the booking form).
const AVATAR_COLOURS = ['#d96b2b', '#7c4dff', '#1e9d7a', '#c2185b', '#1976d2', '#8d6e63'];
function initialsAvatar(name, i){
  const initials = name.split(' ').filter(Boolean).map(w => w[0]).join('').slice(0, 2).toUpperCase();
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" viewBox="0 0 400 400">
    <rect width="400" height="400" fill="${AVATAR_COLOURS[i % AVATAR_COLOURS.length]}"/>
    <text x="50%" y="54%" text-anchor="middle" dominant-baseline="middle" font-family="Inter, Arial, sans-serif" font-size="150" fill="#fff" font-weight="700">${initials}</text>
  </svg>`;
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
}

let stylistsPromise;
function loadStylists(){
  stylistsPromise ||= supabase.rpc('public_stylists')
    .then(({ data, error }) => { if(error) throw error; return data; });
  return stylistsPromise;
}

async function fillTeam(slider){
  try {
    const stylists = await loadStylists();
    const html = stylists.map((p, i) => sliderCard({
      image: p.photo_url || initialsAvatar(`${p.name} ${p.surname}`, i),
      badge: p.title || 'Stylist',
      title: `${p.name} ${p.surname ? p.surname.charAt(0) + '.' : ''}`.trim(),
      href: 'about.php',
    })).join('');
    fillSlider(slider, html, 'Our team will be listed here soon.');
  } catch(err){
    console.error('Team', err);
    fillSlider(slider, '', 'Could not load the team. Please refresh.');
  }
}

async function fillStylistCount(el){
  try {
    el.textContent = String((await loadStylists()).length);
  } catch(err){
    console.error('Stylist count', err);
  }
}

const featured = document.getElementById('featuredStyles');
if(featured) fillFeatured(featured);

const deals = document.getElementById('dealsSlider');
if(deals) fillDeals(deals);

const team = document.getElementById('teamSlider');
if(team) fillTeam(team);
document.querySelectorAll('[data-stat="stylists"]').forEach(fillStylistCount);

// Small style for the empty/error message inside a section.
const style = document.createElement('style');
style.textContent = '.site-content-empty{ color: var(--text-dim); font-size: 14px; padding: 12px 4px; }';
document.head.appendChild(style);
