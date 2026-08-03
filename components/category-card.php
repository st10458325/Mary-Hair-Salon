<?php
/**
 * components/category-card.php
 * ---------------------------------------------------------------
 * Renders one glass category card for the top-level services grid.
 * Shows a thumbnail image with the category icon badged over its
 * bottom-right corner. Clicking it is handled by services.php's
 * script (it listens for clicks on [data-category-card]) — this
 * component only renders markup + its own styling, no page logic.
 *
 * Usage:
 *   require_once __DIR__ . '/components/category-card.php';
 *   render_category_card($category);
 *
 * Expected $category keys:
 *   id     string  e.g. "braids"          (required)
 *   label  string  e.g. "Braids"          (required)
 *   desc   string  short one-liner
 *   count  int     number of services inside
 *   image  string  thumbnail URL/path
 *   icon   string  one of: grid|braid|scissors|smile|wig|tag
 * ---------------------------------------------------------------
 */

if (!function_exists('render_category_card')) {

    function category_card_icon(string $icon): string
    {
        $icons = [
            'braid'    => '<path d="M7.5 1v13M4 3c0 2 2 2 2 4s-2 2-2 4M11 3c0 2-2 2-2 4s2 2 2 4" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/>',
            'scissors' => '<circle cx="3.2" cy="3.2" r="1.7" stroke="currentColor" stroke-width="1.2"/><circle cx="3.2" cy="11.8" r="1.7" stroke="currentColor" stroke-width="1.2"/><path d="M4.5 4.3L13 11.5M4.5 10.7L13 3.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>',
            'smile'    => '<circle cx="7.5" cy="7.5" r="6.3" stroke="currentColor" stroke-width="1.2"/><path d="M4.8 9c.6 1 1.6 1.6 2.7 1.6S9.6 10 10.2 9" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/><circle cx="5.4" cy="6.2" r="0.8" fill="currentColor"/><circle cx="9.6" cy="6.2" r="0.8" fill="currentColor"/>',
            'wig'      => '<path d="M7.5 2c-3 0-5 2.2-5 5.2 0 1.8.6 3 1.2 4.3.2.5.8.6 1-.1l.5-1.6M7.5 2c3 0 5 2.2 5 5.2 0 1.8-.6 3-1.2 4.3-.2.5-.8.6-1-.1l-.5-1.6" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>',
            'tag'      => '<path d="M2 7.6V3.5C2 2.7 2.7 2 3.5 2h4.1c.4 0 .8.2 1.1.4l5 5c.6.6.6 1.5 0 2.1l-3.6 3.6c-.6.6-1.5.6-2.1 0l-5-5C2.2 7.8 2 7.4 2 7z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/><circle cx="5.2" cy="5.2" r="0.9" fill="currentColor"/>',
            'grid'     => '<rect x="1" y="1" width="5.5" height="5.5" rx="1" fill="currentColor"/><rect x="8" y="1" width="5.5" height="5.5" rx="1" fill="currentColor"/><rect x="1" y="8" width="5.5" height="5.5" rx="1" fill="currentColor"/><rect x="8" y="8" width="5.5" height="5.5" rx="1" fill="currentColor"/>',
        ];
        return $icons[$icon] ?? $icons['grid'];
    }

    function render_category_card(array $category): void
    {
        $id    = htmlspecialchars($category['id']    ?? '');
        $label = htmlspecialchars($category['label']  ?? 'Category');
        $desc  = htmlspecialchars($category['desc']   ?? '');
        $count = (int)($category['count'] ?? 0);
        $image = htmlspecialchars($category['image']  ?? '');
        $icon  = $category['icon'] ?? 'grid';
        ?>
        <button type="button"
                class="category-card glass"
                data-category-card
                data-category-id="<?= $id ?>">

            <div class="category-card__thumb">
                <?php if ($image !== ''): ?>
                    <img src="<?= $image ?>" alt="<?= $label ?>" loading="lazy">
                <?php endif; ?>
                <span class="category-card__icon">
                    <svg width="18" height="18" viewBox="0 0 15 15" fill="none"><?= category_card_icon($icon) ?></svg>
                </span>
            </div>

            <div class="category-card__info">
                <span class="category-card__label"><?= $label ?></span>
                <?php if ($desc !== ''): ?>
                    <span class="category-card__desc"><?= $desc ?></span>
                <?php endif; ?>
                <span class="category-card__count"><?= $count ?> service<?= $count === 1 ? '' : 's' ?></span>
            </div>

            <span class="category-card__arrow" aria-hidden="true">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 8h9M8 3.5L12.5 8 8 12.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
            </span>
        </button>
        <?php
    }
}
?>
<style>
  .category-card{
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: stretch;
    text-align: left;
    border-radius: var(--radius-lg);
    background: var(--glass-bg);
    overflow: hidden;
    transition: background .25s ease, border-color .25s ease, transform .25s ease, box-shadow .25s ease;
  }
  .category-card:hover, .category-card:focus-visible{
    background: var(--glass-bg-hover);
    border-color: var(--orange-glow);
    transform: translateY(-4px);
    box-shadow: 0 20px 40px -20px rgba(0,0,0,0.6);
  }

  .category-card__thumb{
    position: relative;
    aspect-ratio: 4 / 3;
    background: var(--bg-elevated);
  }
  .category-card__thumb img{
    width: 100%; height: 100%;
    object-fit: cover;
    filter: brightness(0.8) saturate(0.9);
    transition: transform .4s ease, filter .3s ease;
  }
  .category-card:hover .category-card__thumb img{
    transform: scale(1.06);
    filter: brightness(0.92) saturate(1);
  }

  /* icon badge, overlaid on the image's bottom-right corner */
  .category-card__icon{
    position: absolute;
    right: 12px;
    bottom: -16px;
    width: 40px; height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: var(--orange);
    color: #fff;
    border: 3px solid var(--bg-elevated);
    box-shadow: 0 8px 18px -6px rgba(0,0,0,0.6);
    z-index: 2;
  }

  .category-card__info{
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 26px 20px 16px;
  }
  .category-card__label{
    font-family: var(--font-display);
    font-size: 19px;
    font-weight: 600;
  }
  .category-card__desc{
    font-size: 12.5px;
    color: var(--text-dim);
    line-height: 1.5;
  }
  .category-card__count{
    font-size: 11.5px;
    font-weight: 600;
    letter-spacing: 0.4px;
    color: var(--orange-light);
    margin-top: 4px;
  }

  .category-card__arrow{
    position: absolute;
    right: 18px;
    top: 18px;
    width: 30px; height: 30px;
    display: flex; align-items: center; justify-content: center;
    border-radius: 50%;
    background: rgba(11,9,8,0.55);
    color: #fff;
    transition: transform .25s ease;
  }
  .category-card:hover .category-card__arrow{ transform: translateX(3px); }
</style>
