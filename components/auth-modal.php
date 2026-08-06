<?php
/**
 * components/auth-modal.php
 * ---------------------------------------------------------------
 * Modal overlay wrapping components/auth-form.php. Include it ONCE
 * per page (it's hidden by default), then open it from anywhere:
 *
 *   window.openAuthModal('login');   // or 'signup'
 *
 * navigation.php's account icon already calls this. Links inside
 * auth-form.php with data-auth-switch="login|signup" are
 * intercepted here to flip tabs instead of navigating away.
 * ---------------------------------------------------------------
 */
require_once __DIR__ . '/auth-form.php';
?>
<div class="auth-modal" id="authModal" data-mode="login" aria-hidden="true">
  <div class="auth-modal__backdrop" data-modal-close></div>

  <div class="auth-modal__panel glass" role="dialog" aria-modal="true" aria-labelledby="authModalTitle">
    <button type="button" class="auth-modal__close" data-modal-close aria-label="Close">
      <svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M4 4l10 10M14 4L4 14" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
    </button>

    <div class="auth-modal__tabs" role="tablist">
      <button type="button" class="auth-modal__tab" data-auth-tab="login" role="tab">Log In</button>
      <button type="button" class="auth-modal__tab" data-auth-tab="signup" role="tab">Sign Up</button>
      <span class="auth-modal__tab-indicator" aria-hidden="true"></span>
    </div>

    <h2 id="authModalTitle" class="visually-hidden">Log in or create an account</h2>

    <div class="auth-modal__panes">
      <div class="auth-modal__pane" data-auth-pane="login">
        <?php render_auth_form('login'); ?>
      </div>
      <div class="auth-modal__pane" data-auth-pane="signup">
        <?php render_auth_form('signup'); ?>
      </div>
    </div>
  </div>
</div>

<style>
  .auth-modal{
    position: fixed;
    inset: 0;
    z-index: 500;
    display: none;
    align-items: center;
    justify-content: center;
    padding: 24px;
  }
  .auth-modal.is-open{ display: flex; }

  .auth-modal__backdrop{
    position: absolute;
    inset: 0;
    background: rgba(6, 4, 3, 0.72);
    backdrop-filter: blur(4px);
  }

  .auth-modal__panel{
    position: relative;
    z-index: 1;
    width: 100%;
    max-width: 500px;
    max-height: 88vh;
    overflow-y: auto;
    border-radius: var(--radius-lg);
    background: var(--bg-elevated);
    border: 1px solid var(--glass-border);
    padding: 30px 28px 26px;
    animation: auth-rise .28s ease;
  }
  @keyframes auth-rise{
    from{ opacity: 0; transform: translateY(16px) scale(0.98); }
    to{ opacity: 1; transform: translateY(0) scale(1); }
  }

  .auth-modal__close{
    position: absolute;
    top: 16px; right: 16px;
    width: 32px; height: 32px;
    display: flex; align-items: center; justify-content: center;
    border-radius: 50%;
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    color: var(--text-dim);
  }
  .auth-modal__close:hover{ color: var(--orange-light); border-color: var(--orange); }

  .auth-modal__tabs{
    position: relative;
    display: grid;
    grid-template-columns: 1fr 1fr;
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    border-radius: 999px;
    padding: 4px;
    margin-bottom: 24px;
  }
  .auth-modal__tab{
    position: relative;
    z-index: 1;
    background: none;
    border: none;
    padding: 10px;
    font-size: 13.5px;
    font-weight: 700;
    color: var(--text-dim);
    border-radius: 999px;
    transition: color .2s ease;
  }
  .auth-modal__tab[aria-selected="true"]{ color: #fff; }
  .auth-modal__tab-indicator{
    position: absolute;
    top: 4px; left: 4px;
    width: calc(50% - 4px);
    height: calc(100% - 8px);
    border-radius: 999px;
    background: linear-gradient(135deg, var(--orange-light), var(--orange));
    transition: transform .25s ease;
  }
  .auth-modal[data-mode="signup"] .auth-modal__tab-indicator{
    transform: translateX(100%);
  }

  .auth-modal__pane{ display: none; }
  .auth-modal[data-mode="login"] .auth-modal__pane[data-auth-pane="login"]{ display: block; }
  .auth-modal[data-mode="signup"] .auth-modal__pane[data-auth-pane="signup"]{ display: block; }

  @media (max-width: 560px){
    .auth-modal{ padding: 14px; }
    .auth-modal__panel{ padding: 26px 20px 22px; }
  }
</style>

<script>
(function(){
  const modal = document.getElementById('authModal');
  let lastFocused = null;

  function setMode(mode){
    modal.dataset.mode = mode;
    modal.querySelectorAll('[data-auth-tab]').forEach(tab =>
      tab.setAttribute('aria-selected', String(tab.dataset.authTab === mode))
    );
  }

  window.openAuthModal = function(mode){
    setMode(mode === 'signup' ? 'signup' : 'login');
    lastFocused = document.activeElement;
    modal.classList.add('is-open');
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
    modal.querySelector(`[data-auth-pane="${modal.dataset.mode}"] input`)?.focus();
  };

  function closeModal(){
    modal.classList.remove('is-open');
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
    if(lastFocused) lastFocused.focus();
  }

  modal.querySelectorAll('[data-modal-close]').forEach(el => el.addEventListener('click', closeModal));
  document.addEventListener('keydown', (e) => {
    if(e.key === 'Escape' && modal.classList.contains('is-open')) closeModal();
  });

  modal.querySelectorAll('[data-auth-tab]').forEach(tab =>
    tab.addEventListener('click', () => setMode(tab.dataset.authTab))
  );

  // Intercept "Create an account" / "Log in" links inside auth-form
  // so they flip tabs instead of navigating away from the modal.
  modal.addEventListener('click', (e) => {
    const link = e.target.closest('[data-auth-switch]');
    if(!link) return;
    e.preventDefault();
    setMode(link.dataset.authSwitch);
    modal.querySelector(`[data-auth-pane="${modal.dataset.mode}"] input`)?.focus();
  });

  setMode('login');
})();
</script>
