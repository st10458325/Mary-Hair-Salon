<?php
/**
 * components/featured-style.php
 * ---------------------------------------------------------------
 * A row of glass image cards with a hover reveal — used on the
 * homepage to spotlight a few signature looks.
 *
 * Usage:
 *   require_once __DIR__ . '/components/featured-style.php';
 *   render_featured_styles([
 *     'eyebrow' => 'Signature Looks',
 *     'title'   => 'Featured Styles',
 *     'items'   => [
 *        ['image' => 'assets/images/box-braids.jpg', 'label' => 'Box Braids', 'href' => 'services.php'],
 *        ...
 *     ],
 *   ]);
 * ---------------------------------------------------------------
 */

if (!function_exists('render_featured_styles')) {
    function render_featured_styles(array $opts): void
    {
        $eyebrow = htmlspecialchars($opts['eyebrow'] ?? '');
        $title   = htmlspecialchars($opts['title']   ?? 'Featured Styles');
        $items   = $opts['items'] ?? [];
        ?>
        <section class="featured-style">
            <div class="featured-style__inner">
                <div class="featured-style__head">
                    <?php if ($eyebrow !== ''): ?><p class="featured-style__eyebrow"><?= $eyebrow ?></p><?php endif; ?>
                    <h2 class="featured-style__title"><?= $title ?></h2>
                </div>

                <div class="featured-style__grid">
                    <?php foreach ($items as $item): ?>
                        <?php
                        $image = htmlspecialchars($item['image'] ?? '');
                        $label = htmlspecialchars($item['label'] ?? '');
                        $href  = htmlspecialchars($item['href']  ?? '#');
                        ?>
                        <a href="<?= $href ?>" class="featured-style__card glass">
                            <img src="<?= $image ?>" alt="<?= $label ?>" loading="lazy">
                            <div class="featured-style__overlay">
                                <span><?= $label ?></span>
                            </div>
                        </a>
                    <?php endforeach; ?>
                </div>
            </div>
        </section>
        <?php
    }
}
?>
<style>
  .featured-style{ padding: 70px 40px; }
  .featured-style__inner{ max-width: 1280px; margin: 0 auto; }
  .featured-style__head{ text-align: center; margin-bottom: 36px; }
  .featured-style__eyebrow{
    font-size: 12px; font-weight: 700; letter-spacing: 3px;
    color: var(--orange-light); margin-bottom: 12px;
  }
  .featured-style__title{
    font-family: var(--font-display);
    font-size: clamp(26px, 3.6vw, 38px);
    font-weight: 600;
  }

  .featured-style__grid{
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
    gap: 22px;
  }
  .featured-style__card{
    position: relative;
    aspect-ratio: 4 / 5;
    border-radius: var(--radius-lg);
    overflow: hidden;
  }
  .featured-style__card img{
    width: 100%; height: 100%;
    object-fit: cover;
    transition: transform .5s ease, filter .4s ease;
    filter: brightness(0.82) saturate(0.85);
  }
  .featured-style__card:hover img{
    transform: scale(1.08);
    filter: brightness(0.95) saturate(1);
  }
  .featured-style__overlay{
    position: absolute;
    inset: 0;
    display: flex;
    align-items: flex-end;
    padding: 20px;
    background: linear-gradient(180deg, transparent 40%, rgba(11,9,8,0.85) 100%);
  }
  .featured-style__overlay span{
    font-family: var(--font-display);
    font-size: 19px;
    font-weight: 600;
    color: #fff;
  }

  @media (max-width: 640px){
    .featured-style{ padding: 52px 20px; }
  }
</style>
