<?php
/**
 * components/button.php
 * ---------------------------------------------------------------
 * Reusable themed button/link. Renders an <a> if href is given,
 * otherwise a <button type="submit|button">.
 *
 * Usage:
 *   require_once __DIR__ . '/components/button.php';
 *   render_button(['label' => 'Book Now', 'href' => 'services.php']);
 *   render_button(['label' => 'Log In', 'variant' => 'ghost', 'type' => 'submit']);
 *
 * Options:
 *   label    string   required
 *   href     string   if set, renders as <a>
 *   variant  string   primary (default) | ghost | glass
 *   size     string   md (default) | sm
 *   icon     string   raw inline SVG placed before the label
 *   attrs    string   extra raw HTML attributes, e.g. 'data-modal-close'
 * ---------------------------------------------------------------
 */

if (!function_exists('render_button')) {
    function render_button(array $opts): void
    {
        $label   = htmlspecialchars($opts['label'] ?? 'Button');
        $href    = $opts['href'] ?? null;
        $variant = $opts['variant'] ?? 'primary';
        $size    = $opts['size'] ?? 'md';
        $icon    = $opts['icon'] ?? '';
        $attrs   = $opts['attrs'] ?? '';
        $type    = htmlspecialchars($opts['type'] ?? 'button');
        $classes = "btn btn--{$variant} btn--{$size}";

        if ($href !== null) {
            $href = htmlspecialchars($href);
            echo "<a href=\"{$href}\" class=\"{$classes}\" {$attrs}>{$icon}{$label}</a>";
        } else {
            echo "<button type=\"{$type}\" class=\"{$classes}\" {$attrs}>{$icon}{$label}</button>";
        }
    }
}
?>
<style>
  .btn{
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    border-radius: 999px;
    font-weight: 700;
    letter-spacing: 0.2px;
    border: 1px solid transparent;
    transition: background .2s ease, border-color .2s ease, transform .15s ease, color .2s ease;
    white-space: nowrap;
  }
  .btn--md{ padding: 13px 26px; font-size: 14.5px; }
  .btn--sm{ padding: 9px 18px; font-size: 12.5px; }

  .btn--primary{
    background: linear-gradient(135deg, var(--orange-light), var(--orange));
    color: #fff;
  }
  .btn--primary:hover{ opacity: 0.92; transform: translateY(-1px); }

  .btn--ghost{
    background: transparent;
    border-color: var(--orange-glow);
    color: var(--text);
  }
  .btn--ghost:hover{ background: var(--orange-soft); border-color: var(--orange); }

  .btn--glass{
    background: var(--glass-bg);
    border-color: var(--glass-border);
    color: var(--text);
    backdrop-filter: blur(var(--glass-blur)) saturate(160%);
    -webkit-backdrop-filter: blur(var(--glass-blur)) saturate(160%);
  }
  .btn--glass:hover{ background: var(--glass-bg-hover); border-color: var(--orange-glow); }
</style>
