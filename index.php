<?php
/**
 * index.php — Crowned Beauty Studio homepage
 * ---------------------------------------------------------------
 * Assembly only — see components/ for the actual pieces and their
 * styling. This file just picks the content that goes into each
 * reusable section.
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
<title>Crowned Beauty Studio — Braids, Cuts & Wigs</title>
<link rel="stylesheet" href="style.css">
</head>
<body>

<?php include __DIR__ . '/components/navigation.php'; ?>

<?php render_hero([
    'eyebrow' => 'Crowned Beauty Studio',
    'title'   => 'Hair That Wears The Crown',
    'text'    => 'Braids, cuts, kids styles and wig installs — booked in minutes, styled with care.',
    'buttons' => [
        ['label' => 'Book Now', 'href' => 'services.php'],
        ['label' => 'Our Story', 'href' => 'about.php', 'variant' => 'ghost'],
    ],
]); ?>

<?php render_featured_styles([
    'eyebrow' => 'Signature Looks',
    'title'   => 'Featured Styles',
    'items'   => [
        ['image' => 'assets/images/kael.png',     'label' => 'Knotless Braids', 'href' => 'services.php'],
        ['image' => 'assets/images/FancyMan.png', 'label' => 'Classic Bob Cut', 'href' => 'services.php'],
        ['image' => 'assets/images/Mahoraga.png', 'label' => 'Lace Front Install', 'href' => 'services.php'],
    ],
]); ?>

<?php render_picture_slider([
    'eyebrow' => 'Limited Time',
    'title'   => 'Discounted Styles & Deals',
    'items'   => [
        ['image' => 'assets/images/kael.png',     'badge' => 'Save 20%', 'badge_variant' => 'deal', 'title' => 'Knotless Braids — was R550, now R440', 'href' => 'services.php'],
        ['image' => 'assets/images/FancyMan.png', 'badge' => 'Save 15%', 'badge_variant' => 'deal', 'title' => 'Classic Bob Cut — was R280, now R238',  'href' => 'services.php'],
        ['image' => 'assets/images/Mahoraga.png', 'badge' => 'Save R80', 'badge_variant' => 'deal', 'title' => 'Lace Front Install — was R650, now R570', 'href' => 'services.php'],
        ['image' => 'assets/images/kael.png',     'badge' => 'Save 10%', 'badge_variant' => 'deal', 'title' => 'Kids Cornrows — was R220, now R198',    'href' => 'services.php'],
    ],
]); ?>

<?php render_picture_slider([
    'eyebrow' => 'Meet The Team',
    'title'   => 'The Hands Behind the Crown',
    'items'   => [
        ['image' => 'assets/images/kael.png',     'badge' => 'Braid Specialist',  'title' => 'Thandiwe M.', 'href' => 'about.php'],
        ['image' => 'assets/images/FancyMan.png', 'badge' => 'Master Barber',      'title' => 'Sipho K.',    'href' => 'about.php'],
        ['image' => 'assets/images/Mahoraga.png', 'badge' => 'Wig Specialist',     'title' => 'Naledi P.',   'href' => 'about.php'],
        ['image' => 'assets/images/kael.png',     'badge' => 'Kids Styling',       'title' => 'Bontle S.',   'href' => 'about.php'],
    ],
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
