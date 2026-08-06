<?php
/**
 * services.php — Mary Hair salon
 * ---------------------------------------------------------------
 * Categories and services now live in Firestore (`categories` and
 * `services` collections) instead of hardcoded PHP arrays — PHP
 * can't read Firestore without the Admin SDK, so this page renders
 * an empty shell and the script below fetches + builds the cards
 * client-side. category-card.php / service-card.php are still
 * require_once'd purely for their <style> blocks; the JS below
 * reproduces their exact markup/classes so that CSS still applies.
 * ---------------------------------------------------------------
 */
require_once __DIR__ . '/components/category-card.php';
require_once __DIR__ . '/components/service-card.php';
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Our Services — Mary Hair salon</title>
<link rel="stylesheet" href="style.css">
<link rel="modulepreload" href="https://www.gstatic.com/firebasejs/12.17.0/firebase-app.js">
<link rel="modulepreload" href="https://www.gstatic.com/firebasejs/12.17.0/firebase-firestore.js">
</head>
<body>

<?php include __DIR__ . '/components/navigation.php'; ?>

<section class="services-hero">
  <div class="glow-field"><span></span><span></span><span></span></div>
  <div class="services-hero__inner">
    <p class="services-hero__eyebrow">Our Services</p>
    <h1 class="services-hero__title">Find Your Perfect Style</h1>
    <p class="services-hero__text">Pick a category to see everything on offer, or search for a style by name.</p>
  </div>
</section>

<main class="services-main" id="servicesRoot" data-view="categories">

  <div class="services-main__inner">

    <?php include __DIR__ . '/components/search-filter.php'; ?>

    <p class="services-status" id="servicesLoading">Loading services…</p>

    <!-- Level 1: category grid, built by JS after Firestore fetch -->
    <div class="categories-grid" id="categoriesGrid"></div>

    <!-- Level 2: one sub-list per category, built by JS -->
    <div id="sublistContainer"></div>

    <!-- Empty state for search with zero matches -->
    <p class="services-empty" id="servicesEmpty" hidden>No services match your search — try a different term.</p>

  </div>
</main>

<?php include __DIR__ . '/components/booking-modal.php'; ?>
<?php include __DIR__ . '/components/auth-modal.php'; ?>
<?php include __DIR__ . '/components/footer.php'; ?>

<style>
  /* Page-level layout only — not component styling */
  .services-hero{
    position: relative;
    overflow: hidden;
    padding: 64px 40px 56px;
    border-bottom: 1px solid var(--glass-border);
    text-align: center;
  }
  .services-hero__inner{ position: relative; z-index: 1; max-width: 640px; margin: 0 auto; }
  .services-hero__eyebrow{
    font-size: 12px; font-weight: 700; letter-spacing: 3px;
    color: var(--orange-light); margin-bottom: 16px;
  }
  .services-hero__title{
    font-family: var(--font-display);
    font-size: clamp(34px, 5vw, 52px);
    font-weight: 600;
    margin-bottom: 14px;
  }
  .services-hero__text{ font-size: 15px; color: var(--text-dim); }

  .services-main__inner{
    max-width: 1280px;
    margin: 0 auto;
    padding: 40px 40px 90px;
  }

  .categories-grid{
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    margin-top: 28px;
    align-items: stretch;
  }

  .service-sublist{ margin-top: 28px; }
  .sublist-header{
    display: flex;
    align-items: center;
    gap: 18px;
    margin-bottom: 20px;
  }
  .sublist-header__back{
    display: flex; align-items: center; gap: 6px;
    padding: 8px 14px;
    border-radius: 999px;
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    color: var(--text-dim);
    font-size: 12.5px;
    font-weight: 600;
    transition: color .2s ease, border-color .2s ease;
  }
  .sublist-header__back:hover{ color: var(--orange-light); border-color: var(--orange); }
  .sublist-header h2{
    font-family: var(--font-display);
    font-size: 24px;
    font-weight: 600;
  }
  .sublist-grid{
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    align-items: stretch;
  }

  .services-empty, .services-status{
    text-align: center;
    padding: 60px 20px;
    color: var(--text-faint);
    font-size: 14px;
  }

  /* ---- View states, driven by data-view on #servicesRoot ---- */
  #servicesRoot[data-view="categories"] .service-sublist{ display: none; }

  #servicesRoot[data-view="category"] #categoriesGrid{ display: none; }
  #servicesRoot[data-view="category"] .service-sublist{ display: none; }
  #servicesRoot[data-view="category"] .service-sublist.is-active{
    display: block;
    animation: sublist-part .35s ease;
  }
  @keyframes sublist-part{
    from{ opacity: 0; transform: translateY(10px) scale(0.99); }
    to{ opacity: 1; transform: translateY(0) scale(1); }
  }

  #servicesRoot[data-view="search"] #categoriesGrid{ display: none; }
  #servicesRoot[data-view="search"] .service-sublist{ display: block; }
  #servicesRoot[data-view="search"] .sublist-header{ display: none; }
  #servicesRoot[data-view="search"] .sublist-grid{
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 20px;
    align-items: stretch;
  }
  #servicesRoot[data-view="search"] .service-card{ display: none; }
  #servicesRoot[data-view="search"] .service-card.is-match{ display: flex; }

  @media (max-width: 640px){
    .services-hero{ padding: 48px 20px 40px; }
    .services-main__inner{ padding: 32px 20px 70px; }
  }
</style>

<script type="module">
  import { db } from '/assets/js/firebase-config.js';
  import { collection, getDocs } from "https://www.gstatic.com/firebasejs/12.17.0/firebase-firestore.js";

  const CATEGORY_ICONS = {
    braid:    '<path d="M7.5 1v13M4 3c0 2 2 2 2 4s-2 2-2 4M11 3c0 2-2 2-2 4s2 2 2 4" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>',
    scissors: '<circle cx="3.2" cy="3.2" r="1.7" stroke="currentColor" stroke-width="1.2"/><circle cx="3.2" cy="11.8" r="1.7" stroke="currentColor" stroke-width="1.2"/><path d="M4.5 4.3L13 11.5M4.5 10.7L13 3.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>',
    smile:    '<circle cx="7.5" cy="7.5" r="6.3" stroke="currentColor" stroke-width="1.2"/><path d="M4.8 9c.6 1 1.6 1.6 2.7 1.6S9.6 10 10.2 9" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/><circle cx="5.4" cy="6.2" r="0.8" fill="currentColor"/><circle cx="9.6" cy="6.2" r="0.8" fill="currentColor"/>',
    wig:      '<path d="M7.5 2c-3 0-5 2.2-5 5.2 0 1.8.6 3 1.2 4.3.2.5.8.6 1-.1l.5-1.6M7.5 2c3 0 5 2.2 5 5.2 0 1.8-.6 3-1.2 4.3-.2.5-.8.6-1-.1l-.5-1.6" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>',
    tag:      '<path d="M2 7.6V3.5C2 2.7 2.7 2 3.5 2h4.1c.4 0 .8.2 1.1.4l5 5c.6.6.6 1.5 0 2.1l-3.6 3.6c-.6.6-1.5.6-2.1 0l-5-5C2.2 7.8 2 7.4 2 7z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/><circle cx="5.2" cy="5.2" r="0.9" fill="currentColor"/>',
    grid:     '<rect x="1" y="1" width="5.5" height="5.5" rx="1" fill="currentColor"/><rect x="8" y="1" width="5.5" height="5.5" rx="1" fill="currentColor"/><rect x="1" y="8" width="5.5" height="5.5" rx="1" fill="currentColor"/><rect x="8" y="8" width="5.5" height="5.5" rx="1" fill="currentColor"/>',
  };
  function categoryIcon(icon){ return CATEGORY_ICONS[icon] || CATEGORY_ICONS.grid; }

  function escapeHtml(str){
    return String(str ?? '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function categoryCardHtml(cat){
    const img = cat.image ? `<img src="${escapeHtml(cat.image)}" alt="${escapeHtml(cat.label)}" loading="lazy">` : '';
    const desc = cat.desc ? `<span class="category-card__desc">${escapeHtml(cat.desc)}</span>` : '';
    const count = cat.count || 0;
    return `
      <button type="button" class="category-card glass" data-category-card data-category-id="${escapeHtml(cat.id)}">
        <div class="category-card__thumb">
          ${img}
          <span class="category-card__icon">
            <svg width="18" height="18" viewBox="0 0 15 15" fill="none">${categoryIcon(cat.icon)}</svg>
          </span>
        </div>
        <div class="category-card__info">
          <span class="category-card__label">${escapeHtml(cat.label)}</span>
          ${desc}
          <span class="category-card__count">${count} service${count === 1 ? '' : 's'}</span>
        </div>
        <span class="category-card__arrow" aria-hidden="true">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 8h9M8 3.5L12.5 8 8 12.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </span>
      </button>
    `;
  }

  function serviceCardHtml(svc){
    const img = svc.image ? `<div class="service-card__thumb"><img src="${escapeHtml(svc.image)}" alt="${escapeHtml(svc.title)}" loading="lazy"></div>` : '';
    const desc = svc.description ? `<p class="service-card__desc">${escapeHtml(svc.description)}</p>` : '';
    const tags = Array.isArray(svc.tags) && svc.tags.length
      ? `<ul class="service-card__tags">${svc.tags.map(t => `<li>${escapeHtml(t)}</li>`).join('')}</ul>`
      : '';
    const searchable = `${svc.title || ''} ${svc.category || ''} ${(svc.tags || []).join(' ')}`.toLowerCase();

    return `
      <article class="service-card glass" data-search="${escapeHtml(searchable)}">
        ${img}
        <div class="service-card__body">
          <h3 class="service-card__title">${escapeHtml(svc.title)}</h3>
          ${desc}
          ${tags}
          <div class="service-card__footer">
            <span class="service-card__price">${escapeHtml(svc.price || '')}</span>
            <button type="button" class="service-card__book" data-book-now
              data-service-title="${escapeHtml(svc.title)}"
              data-service-price="${escapeHtml(svc.price || '')}"
              data-service-category="${escapeHtml(svc.category || '')}">
              Book Now
            </button>
          </div>
        </div>
      </article>
    `;
  }

  function sublistHtml(cat, services){
    return `
      <div class="service-sublist" data-sublist="${escapeHtml(cat.id)}">
        <div class="sublist-header">
          <button type="button" class="sublist-header__back" data-back-to-categories>
            <svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M12 7.5H2M6.5 3L2 7.5 6.5 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
            All categories
          </button>
          <h2>${escapeHtml(cat.label)}</h2>
        </div>
        <div class="sublist-grid">
          ${services.map(serviceCardHtml).join('')}
        </div>
      </div>
    `;
  }

  const root = document.getElementById('servicesRoot');
  const categoriesGrid = document.getElementById('categoriesGrid');
  const sublistContainer = document.getElementById('sublistContainer');
  const empty = document.getElementById('servicesEmpty');
  const loading = document.getElementById('servicesLoading');

  function setView(view){ root.setAttribute('data-view', view); }

  function wireEvents(){
    categoriesGrid.addEventListener('click', (e) => {
      const card = e.target.closest('[data-category-card]');
      if(!card) return;
      const id = card.dataset.categoryId;
      document.querySelectorAll('.service-sublist').forEach(el =>
        el.classList.toggle('is-active', el.dataset.sublist === id)
      );
      setView('category');
      document.querySelector(`.service-sublist[data-sublist="${id}"]`)
        ?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });

    document.addEventListener('click', (e) => {
      if(e.target.closest('[data-back-to-categories]')){
        setView('categories');
        categoriesGrid.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    });

    document.addEventListener('services:search', (e) => {
      const query = e.detail.query;
      if(query === ''){
        setView('categories');
        empty.hidden = true;
        return;
      }
      setView('search');
      let matches = 0;
      document.querySelectorAll('.service-card').forEach(card => {
        const isMatch = card.dataset.search.includes(query);
        card.classList.toggle('is-match', isMatch);
        if(isMatch) matches++;
      });
      empty.hidden = matches !== 0;
    });

    document.addEventListener('click', (e) => {
      const btn = e.target.closest('[data-book-now]');
      if(!btn) return;
      window.openBookingModal({
        title: btn.dataset.serviceTitle,
        price: btn.dataset.servicePrice,
        category: btn.dataset.serviceCategory,
      });
    });
  }

  async function loadCatalog(){
    try {
      const [catSnap, svcSnap] = await Promise.all([
        getDocs(collection(db, 'categories')),
        getDocs(collection(db, 'services')),
      ]);

      const categories = [];
      catSnap.forEach(d => categories.push({ id: d.id, ...d.data() }));

      const services = [];
      svcSnap.forEach(d => services.push({ id: d.id, ...d.data() }));

      const byCategory = {};
      services.forEach(s => {
        if(!byCategory[s.category]) byCategory[s.category] = [];
        byCategory[s.category].push(s);
      });
      categories.forEach(c => { c.count = (byCategory[c.id] || []).length; });

      categoriesGrid.innerHTML = categories.map(categoryCardHtml).join('');
      sublistContainer.innerHTML = categories.map(c => sublistHtml(c, byCategory[c.id] || [])).join('');

      loading.hidden = true;
      wireEvents();
    } catch(err){
      console.error(err);
      loading.textContent = 'Something went wrong loading services. Please refresh.';
    }
  }

  loadCatalog();
</script>

</body>
</html>