<?php
/**
 * about.php — Crowned Beauty Studio
 * ---------------------------------------------------------------
 */
require_once __DIR__ . '/components/hero.php';
require_once __DIR__ . '/components/picture-slider.php';

$stats = [
    ['value' => '7+',  'label' => 'Years Styling'],
    ['value' => '2.4k', 'label' => 'Clients Served'],
    ['value' => '4',   'label' => 'Specialist Stylists'],
    ['value' => '4.9', 'label' => 'Average Rating'],
];
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>About Us — Crowned Beauty Studio</title>
<link rel="stylesheet" href="style.css">
</head>
<body>

<?php include __DIR__ . '/components/navigation.php'; ?>

<?php render_hero([
    'eyebrow' => 'About Us',
    'title'   => 'The Studio Behind the Crown',
    'text'    => 'Crowned Beauty Studio started as a single chair and a waitlist. Today it\'s a team dedicated to one thing: hair that makes you feel like you.',
    'compact' => true,
]); ?>

<section class="about-story">
  <div class="about-story__inner">
    <div class="about-story__text">
      <h2>Our Story</h2>
      <p>What began in a spare room has grown into a full studio, but the approach hasn't changed — every style is hand-parted, every client is heard, and every appointment ends with hair that holds up to real life.</p>
      <p>We specialise in protective braiding, precision cuts, gentle kids' styling and natural-looking wig installs, all under one roof.</p>
    </div>

    <div class="about-story__stats">
      <?php foreach ($stats as $stat): ?>
        <div class="about-stat glass">
          <span class="about-stat__value"><?= htmlspecialchars($stat['value']) ?></span>
          <span class="about-stat__label"><?= htmlspecialchars($stat['label']) ?></span>
        </div>
      <?php endforeach; ?>
    </div>
  </div>
</section>

<?php render_picture_slider([
    'eyebrow' => 'Meet The Team',
    'title'   => 'The Stylists',
    'items'   => [
        ['image' => 'assets/images/kael.png',     'badge' => 'Braid Specialist',  'title' => 'Thandiwe M.', 'href' => '#'],
        ['image' => 'assets/images/FancyMan.png', 'badge' => 'Master Barber',      'title' => 'Sipho K.',    'href' => '#'],
        ['image' => 'assets/images/Mahoraga.png', 'badge' => 'Wig Specialist',     'title' => 'Naledi P.',   'href' => '#'],
        ['image' => 'assets/images/kael.png',     'badge' => 'Kids Styling',       'title' => 'Bontle S.',   'href' => '#'],
    ],
]); ?>

<?php include __DIR__ . '/components/booking-modal.php'; ?>
<?php include __DIR__ . '/components/auth-modal.php'; ?>
<?php include __DIR__ . '/components/footer.php'; ?>

<style>
  .about-story{ padding: 60px 40px; }
  .about-story__inner{
    max-width: 1280px;
    margin: 0 auto;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 50px;
    align-items: center;
  }
  .about-story__text h2{
    font-family: var(--font-display);
    font-size: clamp(24px, 3.2vw, 32px);
    font-weight: 600;
    margin-bottom: 16px;
  }
  .about-story__text p{
    color: var(--text-dim);
    font-size: 14.5px;
    line-height: 1.7;
    margin-bottom: 14px;
  }

  .about-story__stats{
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
  }
  .about-stat{
    padding: 24px 20px;
    border-radius: var(--radius-md);
    text-align: center;
  }
  .about-stat__value{
    display: block;
    font-family: var(--font-display);
    font-size: 30px;
    font-weight: 700;
    color: var(--orange-light);
  }
  .about-stat__label{
    display: block;
    font-size: 11.5px;
    color: var(--text-dim);
    margin-top: 6px;
  }

  @media (max-width: 900px){
    .about-story__inner{ grid-template-columns: 1fr; }
  }
  @media (max-width: 640px){
    .about-story{ padding: 44px 20px; }
  }
</style>

</body>
</html>
