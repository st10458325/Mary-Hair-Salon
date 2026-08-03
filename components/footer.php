<?php
/**
 * components/footer.php
 * Standalone footer. Include with:
 *   <?php include __DIR__ . '/components/footer.php'; ?>
 * ---------------------------------------------------------------
 */
?>
<footer class="site-footer">
  <div class="site-footer__inner">
    <div class="site-footer__brand">
      <span class="site-footer__name">CROWNED</span>
      <span class="site-footer__sub">BEAUTY STUDIO</span>
    </div>

    <div class="site-footer__social">
      <a href="#" aria-label="Instagram" class="glass">
        <svg width="18" height="18" viewBox="0 0 640 640" fill="currentColor"><path d="M476.9 161.1C435 119.1 379.2 96 319.9 96C197.5 96 97.9 195.6 97.9 318C97.9 357.1 108.1 395.3 127.5 429L96 544L213.7 513.1C246.1 530.8 282.6 540.1 319.8 540.1L319.9 540.1C442.2 540.1 544 440.5 544 318.1C544 258.8 518.8 203.1 476.9 161.1zM319.9 502.7C286.7 502.7 254.2 493.8 225.9 477L219.2 473L149.4 491.3L168 423.2L163.6 416.2C145.1 386.8 135.4 352.9 135.4 318C135.4 216.3 218.2 133.5 320 133.5C369.3 133.5 415.6 152.7 450.4 187.6C485.2 222.5 506.6 268.8 506.5 318.1C506.5 419.9 421.6 502.7 319.9 502.7zM421.1 364.5C415.6 361.7 388.3 348.3 383.2 346.5C378.1 344.6 374.4 343.7 370.7 349.3C367 354.9 356.4 367.3 353.1 371.1C349.9 374.8 346.6 375.3 341.1 372.5C308.5 356.2 287.1 343.4 265.6 306.5C259.9 296.7 271.3 297.4 281.9 276.2C283.7 272.5 282.8 269.3 281.4 266.5C280 263.7 268.9 236.4 264.3 225.3C259.8 214.5 255.2 216 251.8 215.8C248.6 215.6 244.9 215.6 241.2 215.6C237.5 215.6 231.5 217 226.4 222.5C221.3 228.1 207 241.5 207 268.8C207 296.1 226.9 322.5 229.6 326.2C232.4 329.9 268.7 385.9 324.4 410C359.6 425.2 373.4 426.5 391 423.9C401.7 422.3 423.8 410.5 428.4 397.5C433 384.5 433 373.4 431.6 371.1C430.3 368.6 426.6 367.2 421.1 364.5z"/></svg>
      </a>
      <a href="#" aria-label="Facebook" class="glass">
        <svg width="18" height="18" viewBox="0 0 640 640" fill="currentColor"><path d="M576 320C576 178.6 461.4 64 320 64C178.6 64 64 178.6 64 320C64 440 146.7 540.8 258.2 568.5L258.2 398.2L205.4 398.2L205.4 320L258.2 320L258.2 286.3C258.2 199.2 297.6 158.8 383.2 158.8C399.4 158.8 427.4 162 438.9 165.2L438.9 236C432.9 235.4 422.4 235 409.3 235C367.3 235 351.1 250.9 351.1 292.2L351.1 320L434.7 320L420.3 398.2L351 398.2L351 574.1C477.8 558.8 576 450.9 576 320z"/></svg>
      </a>
      <a href="#" aria-label="Pinterest" class="glass">
        <svg width="18" height="18" viewBox="0 0 640 640" fill="currentColor"><path d="M544.5 273.9C500.5 274 457.5 260.3 421.7 234.7L421.7 413.4C421.7 446.5 411.6 478.8 392.7 506C373.8 533.2 347.1 554 316.1 565.6C285.1 577.2 251.3 579.1 219.2 570.9C187.1 562.7 158.3 545 136.5 520.1C114.7 495.2 101.2 464.1 97.5 431.2C93.8 398.3 100.4 365.1 116.1 336C131.8 306.9 156.1 283.3 185.7 268.3C215.3 253.3 248.6 247.8 281.4 252.3L281.4 342.2C266.4 337.5 250.3 337.6 235.4 342.6C220.5 347.6 207.5 357.2 198.4 369.9C189.3 382.6 184.4 398 184.5 413.8C184.6 429.6 189.7 444.8 199 457.5C208.3 470.2 221.4 479.6 236.4 484.4C251.4 489.2 267.5 489.2 282.4 484.3C297.3 479.4 310.4 469.9 319.6 457.2C328.8 444.5 333.8 429.1 333.8 413.4L333.8 64L421.8 64C421.7 71.4 422.4 78.9 423.7 86.2C426.8 102.5 433.1 118.1 442.4 131.9C451.7 145.7 463.7 157.5 477.6 166.5C497.5 179.6 520.8 186.6 544.6 186.6L544.6 274z"/></svg>
      </a>
    </div>

    <ul class="site-footer__links">
      <li><a href="index.php">Home</a></li>
      <li><a href="services.php">Services</a></li>
      <li><a href="contact.php">Contact</a></li>
    </ul>
  </div>

  <div class="site-footer__bottom">
    <p>&copy; 2026 Crowned Beauty Studio. All rights reserved.</p>
  </div>
</footer>

<style>
  .site-footer{
    position: relative;
    background: var(--bg-elevated);
    border-top: 1px solid var(--glass-border);
  }
  .site-footer__inner{
    max-width: 1440px;
    margin: 0 auto;
    padding: 56px 40px 32px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 26px;
    text-align: center;
  }
  .site-footer__brand{ line-height: 1.1; }
  .site-footer__name{
    display: block;
    font-family: var(--font-display);
    font-size: 20px;
    font-weight: 600;
    letter-spacing: 0.5px;
  }
  .site-footer__sub{
    display: block;
    font-size: 10px;
    letter-spacing: 3px;
    color: var(--orange-light);
    font-weight: 600;
    margin-top: 2px;
  }

  .site-footer__social{ display: flex; gap: 12px; }
  .site-footer__social a{
    width: 42px; height: 42px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    color: var(--text-dim);
    transition: color .2s ease, border-color .2s ease, transform .2s ease;
  }
  .site-footer__social a:hover{
    color: var(--orange-light);
    border-color: var(--orange);
    transform: translateY(-2px);
  }

  .site-footer__links{ display: flex; gap: 28px; }
  .site-footer__links a{
    font-size: 12.5px;
    font-weight: 600;
    letter-spacing: 0.5px;
    color: var(--text-dim);
    transition: color .2s ease;
  }
  .site-footer__links a:hover{ color: var(--text); }

  .site-footer__bottom{
    border-top: 1px solid var(--glass-border);
    padding: 18px 20px;
    text-align: center;
  }
  .site-footer__bottom p{
    font-size: 12px;
    color: var(--text-faint);
  }

  @media (max-width: 600px){
    .site-footer__links{ flex-direction: column; gap: 14px; }
  }
</style>
