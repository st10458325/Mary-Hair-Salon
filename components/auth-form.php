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
 * No backend yet — both forms just show a placeholder confirmation
 * on submit. Swap that for a real fetch() once auth exists.
 * ---------------------------------------------------------------
 */

if (!function_exists('render_auth_form')) {
    function render_auth_form(string $mode = 'login'): void
    {
        $isLogin = $mode === 'login';
        $formId  = $isLogin ? 'loginForm' : 'signupForm';
        ?>
        <form class="auth-form" id="<?= $formId ?>" data-auth-form="<?= $isLogin ? 'login' : 'signup' ?>">

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

  .auth-form__row{ display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
  @media (max-width: 480px){ .auth-form__row{ grid-template-columns: 1fr; } }

  .auth-form__field{ display: flex; flex-direction: column; gap: 6px; }
  .auth-form__field span{ font-size: 12px; color: var(--text-dim); font-weight: 600; }
  .auth-form__field input{
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

  .auth-form__switch{
    text-align: center;
    font-size: 13px;
    color: var(--text-dim);
  }
  .auth-form__switch a{ color: var(--orange-light); font-weight: 600; }
  .auth-form__switch a:hover{ text-decoration: underline; }
</style>

<script>
(function(){
  // Password show/hide — delegated so it works for every auth-form on the page
  document.addEventListener('click', (e) => {
    const btn = e.target.closest('.auth-form__toggle-pw');
    if(!btn) return;
    const input = btn.previousElementSibling;
    const showing = input.type === 'text';
    input.type = showing ? 'password' : 'text';
    btn.setAttribute('aria-label', showing ? 'Show password' : 'Hide password');
  });

  // Placeholder submit — no backend yet
  document.addEventListener('submit', (e) => {
    const form = e.target.closest('[data-auth-form]');
    if(!form) return;
    e.preventDefault();
    const mode = form.dataset.authForm;
    alert(mode === 'login'
      ? 'Login submitted! (Connect this to your backend to authenticate.)'
      : 'Account created! (Connect this to your backend to save it.)');
  });
})();
</script>
