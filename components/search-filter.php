<?php
/**
 * components/search-filter.php
 * ---------------------------------------------------------------
 * Renders the search bar. It only emits a `services:search` custom
 * event with the query text on every keystroke — it doesn't know
 * about categories, cards, or view state. services.php's script
 * listens for that event and decides what to do with it.
 *
 * Usage:
 *   <?php include __DIR__ . '/components/search-filter.php'; ?>
 * ---------------------------------------------------------------
 */
?>
<div class="search-bar glass" data-search-bar>
  <svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="8" cy="8" r="6" stroke="currentColor" stroke-width="1.6"/><path d="M16 16L12.6 12.6" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
  <input type="text" id="serviceSearch" placeholder="Search services or styles..." autocomplete="off">
  <button type="button" class="search-bar__clear" id="searchClear" hidden aria-label="Clear search">
    <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><path d="M3 3l8 8M11 3l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
  </button>
</div>

<style>
  .search-bar{
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 15px 20px;
    border-radius: var(--radius-md);
    color: var(--text-faint);
  }
  .search-bar input{
    flex: 1;
    background: transparent;
    border: none;
    outline: none;
    color: var(--text);
    font-size: 15px;
  }
  .search-bar input::placeholder{ color: var(--text-faint); }
  .search-bar__clear{
    background: var(--glass-bg-hover);
    border: none;
    border-radius: 50%;
    width: 22px; height: 22px;
    display: flex; align-items: center; justify-content: center;
    color: var(--text-dim);
  }
  .search-bar__clear:hover{ color: var(--orange-light); }
</style>

<script>
(function(){
  const input = document.getElementById('serviceSearch');
  const clearBtn = document.getElementById('searchClear');

  input.addEventListener('input', () => {
    clearBtn.hidden = input.value.length === 0;
    document.dispatchEvent(new CustomEvent('services:search', { detail: { query: input.value.trim().toLowerCase() } }));
  });

  clearBtn.addEventListener('click', () => {
    input.value = '';
    clearBtn.hidden = true;
    input.focus();
    document.dispatchEvent(new CustomEvent('services:search', { detail: { query: '' } }));
  });
})();
</script>
