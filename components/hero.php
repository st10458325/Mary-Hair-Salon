<?php
/**
 * components/hero.php
 * ---------------------------------------------------------------
 * Reusable page-top banner: eyebrow + title + supporting text +
 * optional action buttons, with the ambient glow-field behind it.
 *
 * Usage:
 *   require_once __DIR__ . '/components/hero.php';
 *   render_hero([
 *     'eyebrow' => 'Our Services',
 *     'title'   => 'Find Your Perfect Style',
 *     'text'    => 'Pick a category or search for a style by name.',
 *     'compact' => false, // true = shorter hero for inner pages
 *     'buttons' => [
 *        ['label' => 'Book Now', 'href' => 'services.php', 'variant' => 'primary'],
 *        ['label' => 'Our Story', 'href' => 'about.php', 'variant' => 'ghost'],
 *     ],
 *   ]);
 * ---------------------------------------------------------------
 */

require_once __DIR__ . '/button.php';

if (!function_exists('render_hero')) {
    function render_hero(array $opts): void
    {
        $eyebrow = htmlspecialchars($opts['eyebrow'] ?? '');
        $title   = htmlspecialchars($opts['title']   ?? '');
        $text    = htmlspecialchars($opts['text']    ?? '');
        $compact = !empty($opts['compact']);
        $buttons = $opts['buttons'] ?? [];
        ?>
        <section class="page-hero <?= $compact ? 'page-hero--compact' : '' ?>">
          <div class="glow-field"><span></span><span></span><span></span></div>
          <div class="page-hero__inner">
            <?php if ($eyebrow !== ''): ?><p class="page-hero__eyebrow"><?= $eyebrow ?></p><?php endif; ?>
            <?php if ($title   !== ''): ?><h1 class="page-hero__title"><?= $title ?></h1><?php endif; ?>
            <?php if ($text    !== ''): ?><p class="page-hero__text"><?= $text ?></p><?php endif; ?>
            <?php if (!empty($buttons)): ?>
              <div class="page-hero__actions">
                <?php foreach ($buttons as $btn): render_button($btn); endforeach; ?>
              </div>
            <?php endif; ?>
          </div>
        </section>
        <?php
    }
}
?>
<style>
  .page-hero{
    position: relative;
    overflow: hidden;
    padding: 64px 40px 56px;
    border-bottom: 1px solid var(--glass-border);
    text-align: center;
  }
  .page-hero--compact{ padding: 44px 40px 36px; }
  .page-hero__inner{ position: relative; z-index: 1; max-width: 640px; margin: 0 auto; }
  .page-hero__eyebrow{
    font-size: 12px; font-weight: 700; letter-spacing: 3px;
    color: var(--orange-light); margin-bottom: 16px;
  }
  .page-hero__title{
    font-family: var(--font-display);
    font-size: clamp(32px, 5vw, 48px);
    font-weight: 600;
    margin-bottom: 14px;
    line-height: 1.12;
  }
  .page-hero__text{ font-size: 15px; color: var(--text-dim); line-height: 1.6; }
  .page-hero__actions{
    display: flex;
    justify-content: center;
    gap: 14px;
    margin-top: 26px;
    flex-wrap: wrap;
  }

  @media (max-width: 640px){
    .page-hero{ padding: 48px 20px 40px; }
  }
</style>
