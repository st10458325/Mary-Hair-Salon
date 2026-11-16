<?php
/**
 * components/auth-form.php
 * ---------------------------------------------------------------
 * The actual login/signup form fields — no modal chrome, no page
 * chrome. It's used two ways:
 *   - components/auth-modal.php wraps it in a glass overlay
 *   - login.php / signup.php wrap it in a plain centered panel
 *
 * The "switch mode" link at the bottom carries BOTH a real href
 * (login.php <-> signup.php, works with no JS) and a
 * data-auth-switch attribute (which auth-modal.php's script
 * intercepts to flip tabs in place instead of navigating).
 *
 * Usage:
 *   require_once __DIR__ . '/components/auth-form.php';
 *   render_auth_form('login');   // or 'signup'
 *
 * Uses Supabase Auth (assets/js/supabase-client.js): email + password,
 * and "Continue with Google" (single sign-on). On sign-up the database
 * trigger creates the user's row in `profiles` as a customer.
 * ---------------------------------------------------------------
 */

if (!function_exists('render_auth_form')) {
    function render_auth_form(string $mode = 'login'): void
    {
        $isLogin = $mode === 'login';
        $formId  = $isLogin ? 'loginForm' : 'signupForm';
        ?>
        <form class="auth-form" id="<?= $formId ?>" method="post" action="" data-auth-form="<?= $isLogin ? 'login' : 'signup' ?>">

            <?php if (!$isLogin): ?>
                <div class="auth-form__row">
                    <label class="auth-form__field">
                        <span>Name</span>
                        <input type="text" name="name" placeholder="Enter your name" required>
                    </label>
                    <label class="auth-form__field">
                        <span>Surname</span>
                        <input type="text" name="surname" placeholder="Enter your surname" required>
                    </label>
                </div>
            <?php endif; ?>

            <label class="auth-form__field">
                <span>Email</span>
                <input type="email" name="email" placeholder="you@example.com" required>
            </label>

            <?php if (!$isLogin): ?>
                <label class="auth-form__field">
                    <span>Contact (Cell No. / WhatsApp)</span>
                    <input type="tel" name="contact" placeholder="e.g. 081 234 5678" required>
                </label>
            <?php endif; ?>

            <label class="auth-form__field auth-form__field--password">
                <span>Password</span>
                <span class="auth-form__password-wrap">
                    <input type="password" name="password" placeholder="••••••••" minlength="8" required>
                    <button type="button" class="auth-form__toggle-pw" aria-label="Show password">
                        <svg width="17" height="17" viewBox="0 0 17 17" fill="none"><path d="M1 8.5S3.7 3 8.5 3s7.5 5.5 7.5 5.5-2.7 5.5-7.5 5.5S1 8.5 1 8.5z" stroke="currentColor" stroke-width="1.3"/><circle cx="8.5" cy="8.5" r="2.2" stroke="currentColor" stroke-width="1.3"/></svg>
                    </button>
                </span>
            </label>

            <?php if (!$isLogin): ?>
                <label class="auth-form__field">
                    <span>Confirm Password</span>
                    <input type="password" name="confirm_password" placeholder="••••••••" minlength="8" required>
                </label>
            <?php endif; ?>

            <?php if ($isLogin): ?>
                <div class="auth-form__meta">
                    <label class="auth-form__checkbox">
                        <input type="checkbox" name="remember">
                        <span>Remember me</span>
                    </label>
                    <a href="forgot-password.php" class="auth-form__forgot">Forgot password?</a>
                </div>
            <?php else: ?>
                <label class="auth-form__checkbox">
                    <input type="checkbox" name="terms" required>
                    <span>I agree to the Terms of Service and Privacy Policy</span>
                </label>
            <?php endif; ?>

            <button type="submit" class="auth-form__submit">
                <?= $isLogin ? 'Log In' : 'Create Account' ?>
            </button>

            <div class="auth-form__divider"><span>or</span></div>

            <button type="button" class="auth-form__google" data-google-signin>
                <svg width="18" height="18" viewBox="0 0 18 18" aria-hidden="true"><path fill="#4285F4" d="M17.64 9.2c0-.64-.06-1.25-.16-1.84H9v3.48h4.84a4.14 4.14 0 01-1.8 2.72v2.26h2.92c1.7-1.57 2.68-3.88 2.68-6.62z"/><path fill="#34A853" d="M9 18c2.43 0 4.47-.8 5.96-2.18l-2.92-2.26c-.8.54-1.84.86-3.04.86-2.34 0-4.32-1.58-5.03-3.7H.96v2.33A9 9 0 009 18z"/><path fill="#FBBC05" d="M3.97 10.72A5.4 5.4 0 013.68 9c0-.6.1-1.18.29-1.72V4.95H.96A9 9 0 000 9c0 1.45.35 2.83.96 4.05l3.01-2.33z"/><path fill="#EA4335" d="M9 3.58c1.32 0 2.5.45 3.44 1.35l2.58-2.58A9 9 0 009 0 9 9 0 00.96 4.95l3.01 2.33C4.68 5.16 6.66 3.58 9 3.58z"/></svg>
                Continue with Google
            </button>

            <p class="auth-form__switch">
                <?php if ($isLogin): ?>
                    New to Mary Hair salon?
                    <a href="signup.php" data-auth-switch="signup">Create an account</a>
                <?php else: ?>
                    Already have an account?
                    <a href="login.php" data-auth-switch="login">Log in</a>
                <?php endif; ?>
            </p>
        </form>
        <?php
    }
}
?>
<style>
  .auth-form{ display: flex; flex-direction: column; gap: 16px; width: 100%; }

  .auth-form__row{ display: grid; grid-template-columns: minmax(0, 1fr) minmax(0, 1fr); gap: 16px; }
  @media (max-width: 480px){ .auth-form__row{ grid-template-columns: 1fr; } }

  .auth-form__field{ display: flex; flex-direction: column; gap: 6px; min-width: 0; }
  .auth-form__field span{ font-size: 12px; color: var(--text-dim); font-weight: 600; }
  .auth-form__field input{
    width: 100%;
    min-width: 0;
    box-sizing: border-box;
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    border-radius: var(--radius-sm);
    padding: 11px 13px;
    font-size: 14px;
    outline: none;
    transition: border-color .15s ease;
  }
  .auth-form__field input:focus{ border-color: var(--orange); }

  .auth-form__password-wrap{ position: relative; display: flex; }
  .auth-form__password-wrap input{ width: 100%; padding-right: 40px; }
  .auth-form__toggle-pw{
    position: absolute;
    right: 10px; top: 50%;
    transform: translateY(-50%);
    background: none; border: none;
    color: var(--text-faint);
  }
  .auth-form__toggle-pw:hover{ color: var(--orange-light); }

  .auth-form__meta{
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 12.5px;
  }
  .auth-form__forgot{ color: var(--orange-light); font-weight: 600; }
  .auth-form__forgot:hover{ text-decoration: underline; }

  .auth-form__checkbox{
    display: flex;
    align-items: flex-start;
    gap: 9px;
    font-size: 12.5px;
    color: var(--text-dim);
    line-height: 1.4;
  }
  .auth-form__checkbox input{ margin-top: 2px; accent-color: var(--orange); }

  .auth-form__submit{
    margin-top: 4px;
    padding: 14px;
    border-radius: var(--radius-sm);
    border: none;
    background: linear-gradient(135deg, var(--orange-light), var(--orange));
    color: #fff;
    font-size: 15px;
    font-weight: 700;
    transition: opacity .15s ease;
  }
  .auth-form__submit:hover{ opacity: 0.92; }

  .auth-form__divider{
    display: flex; align-items: center; gap: 10px;
    font-size: 12px; color: var(--text-faint);
  }
  .auth-form__divider::before, .auth-form__divider::after{
    content: ""; flex: 1; height: 1px; background: var(--glass-border);
  }
  .auth-form__google{
    display: flex; align-items: center; justify-content: center; gap: 10px;
    padding: 12px;
    border-radius: var(--radius-sm);
    border: 1px solid var(--glass-border);
    background: var(--glass-bg);
    color: inherit;
    font-size: 14px;
    font-weight: 600;
    transition: border-color .15s ease;
  }
  .auth-form__google:hover{ border-color: var(--orange); }
  .auth-form__google:disabled{ opacity: .6; }

  .auth-form__switch{
    text-align: center;
    font-size: 13px;
    color: var(--text-dim);
  }
  .auth-form__switch a{ color: var(--orange-light); font-weight: 600; }
  .auth-form__switch a:hover{ text-decoration: underline; }
  .auth-form__error{
    font-size: 12.5px;
    color: #e0475a;
    background: rgba(224,71,90,0.12);
    border: 1px solid rgba(224,71,90,0.3);
    border-radius: var(--radius-sm);
    padding: 10px 13px;
    }
    .auth-form__error[hidden]{ display: none; }
    .auth-form__error--info{
      color: #2e9e6b;
      background: rgba(46,158,107,0.12);
      border-color: rgba(46,158,107,0.3);
    }
</style>

<script type="module">
  import { supabase, friendlyError } from './assets/js/supabase-client.js';

  // Password show/hide — delegated so it works for every auth-form on the page
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('.auth-form__toggle-pw');
    if(!btn) return;
    const input = btn.previousElementSibling;
    const showing = input.type === 'text';
    input.type = showing ? 'password' : 'text';
    btn.setAttribute('aria-label', showing ? 'Show password' : 'Hide password');
  });

  function setFormError(form, message, kind = 'error'){
    let errorEl = form.querySelector('.auth-form__error');
    if(!errorEl){
      errorEl = document.createElement('p');
      errorEl.className = 'auth-form__error';
      form.querySelector('.auth-form__submit').insertAdjacentElement('beforebegin', errorEl);
    }
    errorEl.textContent = message;
    errorEl.classList.toggle('auth-form__error--info', kind === 'info');
    errorEl.hidden = !message;
  }

  function setFormLoading(form, loading){
    const btn = form.querySelector('.auth-form__submit');
    btn.dataset.originalLabel = btn.dataset.originalLabel || btn.textContent;
    btn.disabled = loading;
    btn.textContent = loading ? 'Please wait…' : btn.dataset.originalLabel;
  }

  // ---- Google single sign-on ----
  // Redirects to Google, then back to bookings.php already logged in.
  // The database trigger creates the profile on the first Google login.
  document.addEventListener('click', async (e) => {
    const btn = e.target.closest('[data-google-signin]');
    if(!btn) return;
    const form = btn.closest('[data-auth-form]');
    btn.disabled = true;
    const { error } = await supabase.auth.signInWithOAuth({
      provider: 'google',
      options: { redirectTo: new URL('bookings.php', window.location.href).href },
    });
    if(error){
      console.error(error);
      if(form) setFormError(form, friendlyError(error));
      btn.disabled = false;
    }
  });

  // ---- Email + password ----
  document.addEventListener('submit', async (e) => {
    const form = e.target.closest('[data-auth-form]');
    if(!form) return;
    e.preventDefault();

    const mode = form.dataset.authForm;
    const data = new FormData(form);
    setFormError(form, '');

    if(mode === 'signup' && data.get('password') !== data.get('confirm_password')){
      setFormError(form, "Passwords don't match.");
      return;
    }

    setFormLoading(form, true);

    try {
      const email = String(data.get('email')).trim();
      const password = String(data.get('password'));

      if(mode === 'login'){
        const { error } = await supabase.auth.signInWithPassword({ email, password });
        if(error) throw error;
      } else {
        // first_name / surname / contact are stored with the account; the
        // on_auth_user_created trigger copies them into the profiles table.
        const { data: result, error } = await supabase.auth.signUp({
          email,
          password,
          options: {
            data: {
              first_name: String(data.get('name')).trim(),
              surname: String(data.get('surname')).trim(),
              contact: String(data.get('contact')).trim(),
            },
            emailRedirectTo: new URL('bookings.php', window.location.href).href,
          },
        });
        if(error) throw error;

        // If "Confirm email" is switched on in Supabase there is no session yet.
        if(!result.session){
          setFormError(form, 'Account created! Check your email and click the link to confirm it, then log in.', 'info');
          setFormLoading(form, false);
          form.reset();
          return;
        }
      }

      window.location.href = 'bookings.php';
    } catch (err) {
      console.error(err);
      setFormError(form, friendlyError(err));
      setFormLoading(form, false);
    }
  });
</script>
