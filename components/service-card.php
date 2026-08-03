<?php
/**
 * components/service-card.php
 * ---------------------------------------------------------------
 * Renders one glass service card, shown inside an expanded
 * category's sub-list. The "Book Now" button carries data-*
 * attributes; services.php's script reads them to pre-fill the
 * booking modal — this component holds no booking logic itself.
 *
 * Usage:
 *   require_once __DIR__ . '/components/service-card.php';
 *   render_service_card($service);
 *
 * Expected $service keys (all optional except title):
 *   title         string   e.g. "Box Braids"
 *   category      string   category id this belongs to, e.g. "braids"
 *   price         string   e.g. "From R450"
 *   description   string   short blurb, clamped to 3 lines
 *   tags          array    e.g. ['3-4 hrs', 'All lengths']
 *   image         string   thumbnail URL/path
 * ---------------------------------------------------------------
 */

if (!function_exists('render_service_card')) {

    function render_service_card(array $service): void
    {
        $title       = htmlspecialchars($service['title']       ?? 'Untitled Service');
        $category    = htmlspecialchars($service['category']    ?? '');
        $price       = htmlspecialchars($service['price']       ?? '');
        $description = htmlspecialchars($service['description'] ?? '');
        $tags        = $service['tags']  ?? [];
        $image       = htmlspecialchars($service['image']       ?? '');
        $searchable  = htmlspecialchars(strtolower($title . ' ' . $category . ' ' . implode(' ', $tags)));
        ?>
        <article class="service-card glass" data-search="<?= $searchable ?>">
            <?php if ($image !== ''): ?>
                <div class="service-card__thumb">
                    <img src="<?= $image ?>" alt="<?= $title ?>" loading="lazy">
                </div>
            <?php endif; ?>

            <div class="service-card__body">
                <h3 class="service-card__title"><?= $title ?></h3>

                <?php if ($description !== ''): ?>
                    <p class="service-card__desc"><?= $description ?></p>
                <?php endif; ?>

                <?php if (!empty($tags)): ?>
                    <ul class="service-card__tags">
                        <?php foreach ($tags as $tag): ?>
                            <li><?= htmlspecialchars($tag) ?></li>
                        <?php endforeach; ?>
                    </ul>
                <?php endif; ?>

                <div class="service-card__footer">
                    <span class="service-card__price"><?= $price ?></span>
                    <button type="button"
                            class="service-card__book"
                            data-book-now
                            data-service-title="<?= $title ?>"
                            data-service-price="<?= $price ?>"
                            data-service-category="<?= $category ?>">
                        Book Now
                    </button>
                </div>
            </div>
        </article>
        <?php
    }
}
?>
<style>
  .service-card{
    display: flex;
    flex-direction: column;
    border-radius: var(--radius-lg);
    background: var(--glass-bg);
    overflow: hidden;
    transition: background .25s ease, border-color .25s ease, transform .25s ease;
  }
  .service-card:hover{
    background: var(--glass-bg-hover);
    border-color: var(--orange-glow);
    transform: translateY(-3px);
  }

  .service-card__thumb{
    aspect-ratio: 16 / 10;
    overflow: hidden;
  }
  .service-card__thumb img{
    width: 100%; height: 100%;
    object-fit: cover;
    transition: transform .4s ease;
  }
  .service-card:hover .service-card__thumb img{ transform: scale(1.06); }

  .service-card__body{
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 10px;
    flex: 1;
  }
  .service-card__title{
    font-family: var(--font-display);
    font-size: 18px;
    font-weight: 600;
  }
  .service-card__desc{
    font-size: 13px;
    line-height: 1.55;
    color: var(--text-dim);
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  .service-card__tags{
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }
  .service-card__tags li{
    font-size: 11px;
    font-weight: 600;
    padding: 4px 10px;
    border-radius: 999px;
    background: var(--orange-soft);
    color: var(--orange-light);
  }
  .service-card__footer{
    margin-top: auto;
    padding-top: 8px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  .service-card__price{
    font-size: 13.5px;
    font-weight: 700;
    color: var(--text);
  }
  .service-card__book{
    padding: 9px 18px;
    border: 1px solid var(--orange-glow);
    border-radius: 999px;
    background: var(--orange);
    color: #fff;
    font-size: 12.5px;
    font-weight: 700;
    letter-spacing: 0.2px;
    transition: background .2s ease, transform .15s ease;
  }
  .service-card__book:hover{
    background: var(--orange-light);
    transform: translateY(-1px);
  }
</style>
