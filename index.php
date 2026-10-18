<?php
/**
 * index.php — Mary Hair salon homepage
 * ---------------------------------------------------------------
 * Assembly only — see components/ for the actual pieces and their
 * styling. Featured styles, deals and the team are loaded from the
 * Supabase database by assets/js/site-content.js (the sections below
 * start empty and are filled in by that script).
 * ---------------------------------------------------------------
 */
require_once __DIR__ . '/components/hero.php';
require_once __DIR__ . '/components/featured-style.php';
require_once __DIR__ . '/components/picture-slider.php';
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Mary Hair salon — Braids, Cuts & Wigs</title>
<link rel="stylesheet" href="style.css">
</head>
<body>

<?php include __DIR__ . '/components/navigation.php'; ?>

<?php render_hero([
    'eyebrow' => 'Mary Hair salon',
    'title'   => 'Hair That Wears The Crown',
    'text'    => 'Braids, cuts, kids styles and wig installs — booked in minutes, styled with care.',
    'buttons' => [
        ['label' => 'Book Now', 'href' => 'services.php'],
        ['label' => 'Our Story', 'href' => 'about.php', 'variant' => 'ghost'],
    ],
]); ?>

<?php render_featured_styles([
    'id'      => 'featuredStyles',
    'eyebrow' => 'Signature Looks',
    'title'   => 'Featured Styles',
    'items'   => [],   // filled from the database (services table)
]); ?>

<?php render_picture_slider([
    'id'      => 'dealsSlider',
    'eyebrow' => 'Limited Time',
    'title'   => 'Discounted Styles & Deals',
    'items'   => [],   // filled from the database (services with a discount)
]); ?>

<?php render_picture_slider([
    'id'      => 'teamSlider',
    'eyebrow' => 'Meet The Team',
    'title'   => 'The Hands Behind the Crown',
    'items'   => [],   // filled from the database (stylists)
]); ?>

<section class="home-cta">
  <div class="glow-field"><span></span><span></span><span></span></div>
  <div class="home-cta__inner glass">
    <h2>Ready for your next look?</h2>
    <p>Browse services, pick a time that suits you, and we'll take it from there.</p>
    <?php
    require_once __DIR__ . '/components/button.php';
    render_button(['label' => 'Explore Services', 'href' => 'services.php']);
    ?>
  </div>
</section>

<script type="module" src="assets/js/site-content.js"></script>

<?php include __DIR__ . '/components/booking-modal.php'; ?>
<?php include __DIR__ . '/components/auth-modal.php'; ?>
<?php include __DIR__ . '/components/footer.php'; ?>

<style>
  .home-cta{
    position: relative;
    overflow: hidden;
    padding: 30px 40px 90px;
  }
  .home-cta__inner{
    position: relative;
    z-index: 1;
    max-width: 720px;
    margin: 0 auto;
    text-align: center;
    padding: 48px 36px;
    border-radius: var(--radius-lg);
  }
  .home-cta__inner h2{
    font-family: var(--font-display);
    font-size: clamp(24px, 3.4vw, 32px);
    font-weight: 600;
    margin-bottom: 12px;
  }
  .home-cta__inner p{
    color: var(--text-dim);
    font-size: 14.5px;
    margin-bottom: 24px;
  }

  @media (max-width: 640px){
    .home-cta{ padding: 10px 20px 70px; }
    .home-cta__inner{ padding: 36px 22px; }
  }
</style>

</body>
</html>
