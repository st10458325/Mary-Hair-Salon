<?php
/**
 * services.php — Crowned Beauty Studio
 * ---------------------------------------------------------------
 * This page is an assembly point, not a place to add styling.
 * Every visual piece (nav, footer, cards, search bar, booking
 * modal) is its own standalone component under /components, each
 * carrying its own CSS + JS. This file only:
 *   1. defines the category/service data
 *   2. includes the components
 *   3. runs the small state machine that decides which view
 *      (categories / one category / search results) is showing
 * ---------------------------------------------------------------
 */

require_once __DIR__ . '/components/category-card.php';
require_once __DIR__ . '/components/service-card.php';

// ---- Data -------------------------------------------------------
$categories = [
    ['id' => 'braids',   'label' => 'Braids',       'icon' => 'braid',    'desc' => 'Protective styles, hand-parted for a clean finish.', 'image' => 'assets/images/kael.png'],
    ['id' => 'haircuts', 'label' => 'Hair Cuts',    'icon' => 'scissors', 'desc' => 'Precision cuts and blow-dry finishes.', 'image' => 'assets/images/FancyMan.png'],
    ['id' => 'kids',     'label' => 'Kids Styles',  'icon' => 'smile',    'desc' => 'Gentle, comfortable styles for younger clients.', 'image' => 'assets/images/Mahoraga.png'],
    ['id' => 'wigs',     'label' => 'Wig Services', 'icon' => 'wig',      'desc' => 'Melted installs, closures and custom parts.', 'image' => 'assets/images/kael.png'],
];

$services = [
    ['title' => 'Box Braids',            'category' => 'braids',   'price' => 'From R450', 'description' => 'Classic protective braids, hand-parted for a clean, even finish that lasts for weeks.', 'tags' => ['3-4 hrs', 'All lengths'], 'image' => 'assets/images/kael.png'],
    ['title' => 'Knotless Braids',       'category' => 'braids',   'price' => 'From R550', 'description' => 'A gentler take on box braids with a flatter, more natural-looking root.', 'tags' => ['4-5 hrs', 'Low tension'], 'image' => 'assets/images/kael.png'],
    ['title' => 'Classic Bob Cut',       'category' => 'haircuts', 'price' => 'From R280', 'description' => 'A precision cut and blow-dry finish, tailored to your face shape.', 'tags' => ['45 min'], 'image' => 'assets/images/FancyMan.png'],
    ['title' => 'Layered Cut & Style',   'category' => 'haircuts', 'price' => 'From R320', 'description' => 'Soft, face-framing layers finished with a full blow-out.', 'tags' => ['1 hr'], 'image' => 'assets/images/FancyMan.png'],
    ['title' => 'Kids Cornrows',         'category' => 'kids',     'price' => 'From R220', 'description' => 'Neat, comfortable cornrows sized and paced for younger clients.', 'tags' => ['1-2 hrs', 'Ages 4+'], 'image' => 'assets/images/Mahoraga.png'],
    ['title' => 'Kids Twist Out',        'category' => 'kids',     'price' => 'From R180', 'description' => 'A gentle wash, twist and style, kept simple for little ones.', 'tags' => ['45 min', 'Ages 4+'], 'image' => 'assets/images/Mahoraga.png'],
    ['title' => 'Lace Front Wig Install','category' => 'wigs',     'price' => 'From R650', 'description' => 'Melted, natural-looking install with baby hairs laid and sealed.', 'tags' => ['2 hrs'], 'image' => 'assets/images/kael.png'],
    ['title' => 'Closure Wig Install',   'category' => 'wigs',     'price' => 'From R500', 'description' => 'Secure, glue-free closure install finished with a custom part.', 'tags' => ['1.5 hrs'], 'image' => 'assets/images/kael.png'],
];

// group services by category + attach counts to categories
$byCategory = [];
foreach ($services as $s) {
    $byCategory[$s['category']][] = $s;
}
foreach ($categories as &$cat) {
    $cat['count'] = count($byCategory[$cat['id']] ?? []);
}
unset($cat);
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Our Services — Crowned Beauty Studio</title>
<link rel="stylesheet" href="style.css">
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

    <!-- Level 1: category grid -->
    <div class="categories-grid" id="categoriesGrid">
      <?php foreach ($categories as $cat): ?>
        <?php render_category_card($cat); ?>
      <?php endforeach; ?>
    </div>

    <!-- Level 2: one sub-list per category, hidden until its card is clicked -->
    <?php foreach ($categories as $cat): ?>
      <div class="service-sublist" data-sublist="<?= htmlspecialchars($cat['id']) ?>">
        <div class="sublist-header">
          <button type="button" class="sublist-header__back" data-back-to-categories>
            <svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M12 7.5H2M6.5 3L2 7.5 6.5 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
            All categories
          </button>
          <h2><?= htmlspecialchars($cat['label']) ?></h2>
        </div>
        <div class="sublist-grid">
          <?php foreach ($byCategory[$cat['id']] ?? [] as $service): ?>
            <?php render_service_card($service); ?>
          <?php endforeach; ?>
        </div>
      </div>
    <?php endforeach; ?>

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
    grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
    gap: 20px;
  }

  .services-empty{
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
    display: contents; /* flatten so all matches sit in one implicit flow */
  }
  #servicesRoot[data-view="search"] .service-card{ display: none; }
  #servicesRoot[data-view="search"] .service-card.is-match{ display: flex; }

  @media (max-width: 640px){
    .services-hero{ padding: 48px 20px 40px; }
    .services-main__inner{ padding: 32px 20px 70px; }
  }
</style>

<script>
(function(){
  const root = document.getElementById('servicesRoot');
  const categoriesGrid = document.getElementById('categoriesGrid');
  const empty = document.getElementById('servicesEmpty');

  function setView(view){ root.setAttribute('data-view', view); }

  // Category card clicked -> show that category's sub-list in place
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

  // Back button -> return to category grid
  document.addEventListener('click', (e) => {
    if(e.target.closest('[data-back-to-categories]')){
      setView('categories');
      categoriesGrid.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  });

  // Search (event emitted by components/search-filter.php)
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

  // Book Now -> hand off to the booking modal component
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('[data-book-now]');
    if(!btn) return;
    window.openBookingModal({
      title: btn.dataset.serviceTitle,
      price: btn.dataset.servicePrice,
      category: btn.dataset.serviceCategory
    });
  });
})();
</script>

</body>
</html>
