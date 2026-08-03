<?php
/**
 * components/picture-slider.php
 * ---------------------------------------------------------------
 * Horizontal glass card slider (e.g. stylist spotlights, client
 * results, testimonials). The individual "slide card" markup used
 * to live in its own slider-card.php — it's folded in here since
 * nothing else in the project reuses that card shape.
 *
 * Usage:
 *   require_once __DIR__ . '/components/picture-slider.php';
 *   render_picture_slider([
 *     'eyebrow' => 'Meet The Team',
 *     'title'   => 'The Hands Behind the Crown',
 *     'items'   => [
 *        ['image' => 'assets/images/kael.png', 'badge' => 'Braid Specialist', 'title' => 'Thandiwe M.', 'href' => '#'],
 *        ...
 *     ],
 *   ]);
 * ---------------------------------------------------------------
 */

if (!function_exists('render_picture_slider')) {
    function render_picture_slider(array $opts): void
    {
        $eyebrow = htmlspecialchars($opts['eyebrow'] ?? '');
        $title   = htmlspecialchars($opts['title']   ?? '');
        $items   = $opts['items'] ?? [];
        ?>
        <section class="pic-slider" data-picture-slider>
            <div class="pic-slider__inner">
                <div class="pic-slider__head">
                    <div>
                        <?php if ($eyebrow !== ''): ?><p class="pic-slider__eyebrow"><?= $eyebrow ?></p><?php endif; ?>
                        <?php if ($title !== ''): ?><h2 class="pic-slider__title"><?= $title ?></h2><?php endif; ?>
                    </div>
                    <div class="pic-slider__arrows">
                        <button type="button" class="pic-slider__arrow" data-prev aria-label="Previous slide">
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M10 3L5 8l5 5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        </button>
                        <button type="button" class="pic-slider__arrow" data-next aria-label="Next slide">
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        </button>
                    </div>
                </div>

                <div class="pic-slider__viewport">
                    <div class="pic-slider__track">
                        <?php foreach ($items as $item): ?>
                            <?php
                            $image = htmlspecialchars($item['image'] ?? '');
                            $badge = htmlspecialchars($item['badge'] ?? '');
                            $itemTitle = htmlspecialchars($item['title'] ?? '');
                            $href  = htmlspecialchars($item['href']  ?? '#');
                            ?>
                            <?php $badgeVariant = htmlspecialchars($item['badge_variant'] ?? ''); ?>
                            <a href="<?= $href ?>" class="pic-slider__card glass">
                                <img src="<?= $image ?>" alt="<?= $itemTitle ?>" loading="lazy">
                                <?php if ($badge !== ''): ?><span class="pic-slider__badge<?= $badgeVariant !== '' ? ' pic-slider__badge--' . $badgeVariant : '' ?>"><?= $badge ?></span><?php endif; ?>
                                <h3 class="pic-slider__card-title"><?= $itemTitle ?></h3>
                            </a>
                        <?php endforeach; ?>
                    </div>
                </div>

                <div class="pic-slider__dots"></div>
            </div>
        </section>
        <?php
    }
}
?>
<style>
  .pic-slider{ padding: 70px 40px; }
  .pic-slider__inner{ max-width: 1280px; margin: 0 auto; }
  .pic-slider__head{
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 20px;
    margin-bottom: 30px;
  }
  .pic-slider__eyebrow{
    font-size: 12px; font-weight: 700; letter-spacing: 3px;
    color: var(--orange-light); margin-bottom: 12px;
  }
  .pic-slider__title{
    font-family: var(--font-display);
    font-size: clamp(24px, 3.4vw, 34px);
    font-weight: 600;
  }
  .pic-slider__arrows{ display: flex; gap: 10px; flex-shrink: 0; }
  .pic-slider__arrow{
    width: 40px; height: 40px;
    display: flex; align-items: center; justify-content: center;
    border-radius: 50%;
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    color: var(--text-dim);
    transition: color .2s ease, border-color .2s ease;
  }
  .pic-slider__arrow:hover{ color: var(--orange-light); border-color: var(--orange); }
  .pic-slider__arrow:disabled{ opacity: 0.35; pointer-events: none; }

  .pic-slider__viewport{ overflow: hidden; }
  .pic-slider__track{
    display: flex;
    gap: 20px;
    transition: transform .45s ease;
  }
  .pic-slider__card{
    position: relative;
    flex: 0 0 calc((100% - 40px) / 3);
    border-radius: var(--radius-lg);
    padding: 14px;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  .pic-slider__card img{
    width: 100%;
    aspect-ratio: 4 / 3;
    object-fit: cover;
    border-radius: var(--radius-md);
  }
  .pic-slider__badge{
    align-self: flex-start;
    font-size: 11px;
    font-weight: 700;
    padding: 5px 12px;
    border-radius: 999px;
    background: var(--orange-soft);
    color: var(--orange-light);
  }
  .pic-slider__badge--deal{
    background: rgba(62,207,110,0.16);
    color: #58e08a;
  }
  .pic-slider__card-title{
    font-family: var(--font-display);
    font-size: 17px;
    font-weight: 600;
  }

  .pic-slider__dots{
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-top: 22px;
  }
  .pic-slider__dots button{
    width: 8px; height: 8px;
    border-radius: 999px;
    border: none;
    background: var(--glass-border);
    transition: background .2s ease, width .2s ease;
  }
  .pic-slider__dots button.is-active{ width: 22px; background: var(--orange); }

  @media (max-width: 980px){
    .pic-slider__card{ flex-basis: calc((100% - 20px) / 2); }
  }
  @media (max-width: 640px){
    .pic-slider{ padding: 52px 20px; }
    .pic-slider__card{ flex-basis: 100%; }
    .pic-slider__head{ flex-direction: column; align-items: flex-start; }
  }
</style>

<script>
(function(){
  document.querySelectorAll('[data-picture-slider]').forEach((slider) => {
    const track = slider.querySelector('.pic-slider__track');
    const cards = Array.from(slider.querySelectorAll('.pic-slider__card'));
    const prevBtn = slider.querySelector('[data-prev]');
    const nextBtn = slider.querySelector('[data-next]');
    const dotsWrap = slider.querySelector('.pic-slider__dots');
    if(!cards.length) return;

    let index = 0;

    function visibleCount(){
      const w = slider.offsetWidth;
      if(w <= 640) return 1;
      if(w <= 980) return 2;
      return 3;
    }

    function buildDots(){
      dotsWrap.innerHTML = '';
      const count = Math.max(1, cards.length - visibleCount() + 1);
      for(let i = 0; i < count; i++){
        const dot = document.createElement('button');
        dot.type = 'button';
        dot.setAttribute('aria-label', `Go to slide ${i + 1}`);
        dot.addEventListener('click', () => { index = i; render(); });
        dotsWrap.appendChild(dot);
      }
    }

    function render(){
      const vc = visibleCount();
      const maxIndex = Math.max(0, cards.length - vc);
      index = Math.min(index, maxIndex);

      const cardWidth = cards[0].getBoundingClientRect().width;
      const gap = parseFloat(getComputedStyle(track).gap) || 0;
      track.style.transform = `translateX(-${index * (cardWidth + gap)}px)`;

      dotsWrap.querySelectorAll('button').forEach((d, i) => d.classList.toggle('is-active', i === index));
      prevBtn.disabled = index === 0;
      nextBtn.disabled = index === maxIndex;
    }

    prevBtn.addEventListener('click', () => { index--; render(); });
    nextBtn.addEventListener('click', () => { index++; render(); });

    let resizeTimer;
    window.addEventListener('resize', () => {
      clearTimeout(resizeTimer);
      resizeTimer = setTimeout(() => { buildDots(); render(); }, 150);
    });

    buildDots();
    render();
  });
})();
</script>
