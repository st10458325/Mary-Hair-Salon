<?php
/**
 * components/navigation.php
 * ---------------------------------------------------------------
 * Standalone site navigation. Include it anywhere with:
 *
 *   <?php include __DIR__ . '/components/navigation.php'; ?>
 *
 * Reads global tokens from style.css (--bg, --orange, --text, etc).
 * All navigation-specific CSS + JS lives right here.
 *
 * The account icon opens the auth modal (components/auth-modal.php)
 * if that component is present on the page and its JS has loaded.
 * Its href always points to login.php, so it still works with no
 * JS or on a page that hasn't included the modal.
 *
 * Firebase Auth state (assets/js/firebase-config.js) drives whether
 * the account icon shows "log in" or "logged in" state, and whether
 * the Log Out button is visible. No JS = defaults to logged-out look.
 * ---------------------------------------------------------------
 */
$current_page = basename($_SERVER['PHP_SELF'] ?? '');
if (!function_exists('nav_is_current')) {
    function nav_is_current(string $page, string $current): string
    {
        return $page === $current ? ' aria-current="page"' : '';
    }
}
?>
<header class="site-nav" data-nav>
  <div class="site-nav__inner">
    <a href="index.php" class="site-nav__brand">
      <svg width="30" height="30" viewBox="0 0 34 34" fill="none">
        <path d="M6 24L4 11L11 16L17 6L23 16L30 11L28 24H6Z" stroke="#e8551c" stroke-width="1.6" stroke-linejoin="round"/>
        <path d="M6 24H28" stroke="#e8551c" stroke-width="1.6" stroke-linecap="round"/>
      </svg>
      <span class="site-nav__brand-text">
        <span class="site-nav__name">Mary</span>
        <span class="site-nav__sub">Hair&nbsp;salon</span>
      </span>
    </a>

    <ul class="site-nav__links">
      <li><a href="index.php"<?= nav_is_current('index.php', $current_page) ?>>Home</a></li>
      <li><a href="services.php"<?= nav_is_current('services.php', $current_page) ?>>Services</a></li>
      <li><a href="about.php"<?= nav_is_current('about.php', $current_page) ?>>About</a></li>
      <li><a href="contact.php"<?= nav_is_current('contact.php', $current_page) ?>>Contact</a></li>
    </ul>

    <div class="site-nav__actions">
      <a href="bookings.php" class="site-nav__cta">
        <svg width="15" height="15" viewBox="0 0 16 16" fill="none"><rect x="2" y="3" width="12" height="11" rx="1.5" stroke="currentColor" stroke-width="1.4"/><path d="M2 6.5H14" stroke="currentColor" stroke-width="1.4"/><path d="M5 1.5V4M11 1.5V4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
        My Bookings
      </a>
      <a href="login.php" class="site-nav__account" data-open-auth="login" id="navAccountLink" aria-label="Log in or sign up">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" id="navAccountIcon"><circle cx="8" cy="5.2" r="2.7" stroke="currentColor" stroke-width="1.4"/><path d="M2.3 13.5c1-2.6 3.2-4 5.7-4s4.7 1.4 5.7 4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>
        <span class="site-nav__account-initial" id="navAccountInitial" hidden></span>
      </a>
      <button type="button" class="site-nav__logout" id="navLogoutBtn" hidden aria-label="Log out">
        <svg width="15" height="15" viewBox="0 0 16 16" fill="none"><path d="M6 2H3.5A1.5 1.5 0 002 3.5v9A1.5 1.5 0 003.5 14H6M10.5 11l3-3-3-3M13.3 8H6" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
    </div>

    <button class="site-nav__toggle" aria-label="Open navigation" aria-controls="mobileMenu" aria-expanded="false">
      <span></span><span></span><span></span>
    </button>
  </div>

  <div class="site-nav__mobile" id="mobileMenu" aria-hidden="true">
    <ul>
      <li><a href="index.php">Home</a></li>
      <li><a href="services.php">Services</a></li>
      <li><a href="about.php">About</a></li>
      <li><a href="contact.php">Contact</a></li>
      <li><a href="bookings.php">My Bookings</a></li>
    </ul>
    <ul id="mobileAuthLoggedOut">
      <li><a href="login.php" data-open-auth="login">Log In</a></li>
      <li><a href="signup.php" data-open-auth="signup">Sign Up</a></li>
    </ul>
    <ul id="mobileAuthLoggedIn" hidden>
      <li class="site-nav__mobile-account-name" id="mobileAccountName"></li>
      <li><button type="button" id="mobileLogoutBtn" class="site-nav__mobile-logout">Log Out</button></li>
    </ul>
  </div>
</header>

<style>
  .site-nav{
    position: sticky;
    top: 0;
    z-index: 200;
    background: var(--glass-bg);
    border-bottom: 1px solid var(--glass-border);
    backdrop-filter: blur(var(--glass-blur)) saturate(160%);
    -webkit-backdrop-filter: blur(var(--glass-blur)) saturate(160%);
  }
  .site-nav__inner{
    max-width: 1440px;
    margin: 0 auto;
    padding: 16px 40px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
  }

  .site-nav__brand{ display: flex; align-items: center; gap: 12px; }
  .site-nav__brand-text{ line-height: 1.05; }
  .site-nav__name{
    display: block;
    font-family: var(--font-display);
    font-size: 18px;
    font-weight: 600;
    letter-spacing: 0.5px;
  }
  .site-nav__sub{
    display: block;
    font-size: 9px;
    letter-spacing: 2.5px;
    color: var(--text-faint);
    font-weight: 600;
  }

  .site-nav__links{ display: flex; align-items: center; gap: 34px; }
  .site-nav__links a{
    font-size: 13px;
    font-weight: 600;
    letter-spacing: 0.6px;
    color: var(--text-dim);
    transition: color .2s ease;
  }
  .site-nav__links a:hover{ color: var(--text); }
  .site-nav__links a[aria-current="page"]{ color: var(--orange-light); }

  .site-nav__cta{
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 18px;
    border: 1px solid var(--orange-glow);
    border-radius: 999px;
    font-size: 12.5px;
    font-weight: 600;
    background: var(--orange-soft);
    transition: background .2s ease, border-color .2s ease, transform .15s ease;
    white-space: nowrap;
  }
  .site-nav__cta:hover{
    background: var(--orange-soft);
    border-color: var(--orange);
    transform: translateY(-1px);
  }

  .site-nav__actions{ display: flex; align-items: center; gap: 12px; }

  .site-nav__account{
    width: 38px; height: 38px;
    display: flex; align-items: center; justify-content: center;
    border-radius: 50%;
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    color: var(--text-dim);
    transition: color .2s ease, border-color .2s ease, background .2s ease;
    flex-shrink: 0;
  }
  .site-nav__account:hover{ color: var(--orange-light); border-color: var(--orange); }

  .site-nav__account-initial{
    font-family: var(--font-display);
    font-size: 15px;
    font-weight: 700;
    color: #fff;
  }
  /* When logged in, the whole circle fills with the accent color and shows the initial */
  .site-nav__account.is-authed{
    background: linear-gradient(135deg, var(--orange-light), var(--orange));
    border-color: transparent;
  }

  .site-nav__logout{
    width: 38px; height: 38px;
    display: flex; align-items: center; justify-content: center;
    border-radius: 50%;
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    color: var(--text-dim);
    transition: color .2s ease, border-color .2s ease;
    flex-shrink: 0;
  }
  .site-nav__logout:hover{ color: #e0475a; border-color: #e0475a; }

  .site-nav__toggle{
    display: none;
    flex-direction: column;
    gap: 4px;
    background: none;
    border: none;
    padding: 6px;
  }
  .site-nav__toggle span{
    width: 20px; height: 2px;
    background: var(--text);
    border-radius: 2px;
  }

  .site-nav__mobile{
    display: none;
    border-top: 1px solid var(--glass-border);
  }
  .site-nav__mobile ul{ padding: 8px 24px 18px; }
  .site-nav__mobile ul + ul{
    padding-top: 0;
    border-top: 1px solid var(--glass-border);
    margin: 0 24px;
    padding: 12px 0 18px;
  }
  .site-nav__mobile a{
    display: block;
    padding: 12px 0;
    font-size: 14px;
    font-weight: 600;
    color: var(--text-dim);
  }
  .site-nav__mobile.is-open{ display: block; }
  .site-nav__mobile-account-name{
    padding: 12px 0 4px;
    font-size: 13px;
    font-weight: 700;
    color: var(--orange-light);
  }
  .site-nav__mobile-logout{
    background: none;
    border: none;
    padding: 8px 0;
    font-size: 14px;
    font-weight: 600;
    color: #e0475a;
  }

  @media (max-width: 860px){
    .site-nav__inner{ padding: 14px 20px; }
    .site-nav__links, .site-nav__actions{ display: none; }
    .site-nav__toggle{ display: flex; }
  }
</style>

<script type="module">
  import { auth } from '/assets/js/firebase-config.js';
  import { onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/12.17.0/firebase-auth.js";

  const nav = document.querySelector('[data-nav]');
  if(nav){
    const toggle = nav.querySelector('.site-nav__toggle');
    const menu = nav.querySelector('.site-nav__mobile');

    toggle.addEventListener('click', () => {
      const open = menu.classList.toggle('is-open');
      toggle.setAttribute('aria-expanded', String(open));
      menu.setAttribute('aria-hidden', String(!open));
    });

    menu.querySelectorAll('a, button').forEach(el => el.addEventListener('click', () => {
      menu.classList.remove('is-open');
      toggle.setAttribute('aria-expanded', 'false');
      menu.setAttribute('aria-hidden', 'true');
    }));

    // If components/auth-modal.php is on this page, open it instead of
    // navigating to login.php / signup.php. If it isn't, these are
    // just plain links and the browser navigates normally.
    nav.querySelectorAll('[data-open-auth]').forEach(link => {
      link.addEventListener('click', (e) => {
        if(link.dataset.openAuth == null) return; // logged in — let it navigate normally
        if(typeof window.openAuthModal !== 'function') return;
        e.preventDefault();
        window.openAuthModal(link.dataset.openAuth);
      });
    });

    // ---- Auth state ----
    const accountLink = document.getElementById('navAccountLink');
    const accountIcon = document.getElementById('navAccountIcon');
    const accountInitial = document.getElementById('navAccountInitial');
    const logoutBtn = document.getElementById('navLogoutBtn');
    const mobileLoggedOut = document.getElementById('mobileAuthLoggedOut');
    const mobileLoggedIn = document.getElementById('mobileAuthLoggedIn');
    const mobileAccountName = document.getElementById('mobileAccountName');
    const mobileLogoutBtn = document.getElementById('mobileLogoutBtn');

    function applyAuthState(user){
      if(user){
        const label = (user.displayName || user.email || 'Account').trim();
        accountLink.href = 'bookings.php';
        accountLink.removeAttribute('data-open-auth');
        accountLink.setAttribute('aria-label', `Account: ${label}`);
        accountLink.classList.add('is-authed');
        accountIcon.style.display = 'none';
        accountInitial.textContent = label.charAt(0).toUpperCase();
        accountInitial.hidden = false;
        logoutBtn.hidden = false;

        mobileLoggedOut.hidden = true;
        mobileLoggedIn.hidden = false;
        mobileAccountName.textContent = label;
      } else {
        accountLink.href = 'login.php';
        accountLink.setAttribute('data-open-auth', 'login');
        accountLink.setAttribute('aria-label', 'Log in or sign up');
        accountLink.classList.remove('is-authed');
        accountIcon.style.display = '';
        accountInitial.hidden = true;
        logoutBtn.hidden = true;

        mobileLoggedOut.hidden = false;
        mobileLoggedIn.hidden = true;
      }
    }

    onAuthStateChanged(auth, applyAuthState);

    async function doLogout(){
      await signOut(auth);
      window.location.href = 'index.php';
    }
    logoutBtn.addEventListener('click', doLogout);
    mobileLogoutBtn.addEventListener('click', doLogout);
  }

</script>