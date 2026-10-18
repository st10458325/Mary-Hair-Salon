<?php
/**
 * forgot-password.php — Mary Hair salon
 * ---------------------------------------------------------------
 * Linked from components/auth-form.php's login mode.
 * Step 1: enter email -> Supabase emails a reset link.
 * Step 2: the link brings the user back here (PASSWORD_RECOVERY),
 *         and the "choose a new password" form is shown instead.
 * ---------------------------------------------------------------
 */
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Reset Password — Mary Hair salon</title>
<link rel="stylesheet" href="style.css">
</head>
<body>

<?php include __DIR__ . '/components/navigation.php'; ?>

<main class="auth-page">
  <div class="glow-field"><span></span><span></span><span></span></div>
  <div class="auth-page__panel glass">
    <div class="auth-page__head">
      <h1>Reset Your Password</h1>
      <p>Enter your email and we'll send you a link to reset it.</p>
    </div>

    <form class="auth-form" id="forgotForm" method="post" action="">
      <label class="auth-form__field">
        <span>Email</span>
        <input type="email" name="email" placeholder="you@example.com" required>
      </label>
      <button type="submit" class="auth-form__submit">Send Reset Link</button>
      <p class="auth-form__switch">
        Remembered it? <a href="login.php">Back to Log In</a>
      </p>
    </form>

    <form class="auth-form" id="newPasswordForm" method="post" action="" hidden>
      <label class="auth-form__field">
        <span>New Password</span>
        <input type="password" name="password" placeholder="••••••••" minlength="8" required>
      </label>
      <label class="auth-form__field">
        <span>Confirm New Password</span>
        <input type="password" name="confirm_password" placeholder="••••••••" minlength="8" required>
      </label>
      <button type="submit" class="auth-form__submit">Save New Password</button>
    </form>
  </div>
</main>

<?php include __DIR__ . '/components/footer.php'; ?>

<style>
  .auth-form[hidden]{ display: none !important; }
  .auth-page{
    position: relative;
    overflow: hidden;
    min-height: calc(100vh - 260px);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
  }
  .auth-page__panel{
    position: relative;
    z-index: 1;
    width: 100%;
    max-width: 420px;
    border-radius: var(--radius-lg);
    padding: 36px 32px;
  }
  .auth-page__head{ text-align: center; margin-bottom: 26px; }
  .auth-page__head h1{
    font-family: var(--font-display);
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 6px;
  }
  .auth-page__head p{ font-size: 13.5px; color: var(--text-dim); }

  /* Reuses .auth-form field styling from components/auth-form.php's
     conventions so this one-off form matches without re-including
     the whole component just for a single field. */
  .auth-form{ display: flex; flex-direction: column; gap: 16px; }
  .auth-form__field{ display: flex; flex-direction: column; gap: 6px; }
  .auth-form__field span{ font-size: 12px; color: var(--text-dim); font-weight: 600; }
  .auth-form__field input{
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    border-radius: var(--radius-sm);
    padding: 11px 13px;
    font-size: 14px;
    outline: none;
  }
  .auth-form__field input:focus{ border-color: var(--orange); }
  .auth-form__submit{
    padding: 14px;
    border-radius: var(--radius-sm);
    border: none;
    background: linear-gradient(135deg, var(--orange-light), var(--orange));
    color: #fff;
    font-size: 15px;
    font-weight: 700;
  }
  .auth-form__submit:hover{ opacity: 0.92; }
  .auth-form__switch{ text-align: center; font-size: 13px; color: var(--text-dim); }
  .auth-form__switch a{ color: var(--orange-light); font-weight: 600; }
</style>

<script type="module">
import { supabase, friendlyError } from './assets/js/supabase-client.js';

document.getElementById('forgotForm').addEventListener('submit', async function(e){
  e.preventDefault();
  const form = this;
  const email = String(new FormData(form).get('email') || '').trim();
  const btn = form.querySelector('button[type="submit"]');
  if(btn) btn.disabled = true;
  try {
    // Supabase emails a reset link that brings the user back to this page
    // (add it under Supabase -> Authentication -> URL Configuration -> Redirect URLs).
    const { error } = await supabase.auth.resetPasswordForEmail(email, {
      redirectTo: new URL('forgot-password.php', window.location.href).href,
    });
    if(error) throw error;
    alert('If an account exists for that email, a reset link is on its way. Check your inbox.');
    form.reset();
  } catch(err){
    console.error(err);
    alert(friendlyError(err));
  } finally {
    if(btn) btn.disabled = false;
  }
});

// Step 2: user arrived from the email link -> show the new-password form.
const newPasswordForm = document.getElementById('newPasswordForm');
supabase.auth.onAuthStateChange((event) => {
  if(event !== 'PASSWORD_RECOVERY') return;
  document.getElementById('forgotForm').hidden = true;
  newPasswordForm.hidden = false;
  document.querySelector('.auth-page__head h1').textContent = 'Choose a New Password';
  document.querySelector('.auth-page__head p').textContent = 'Enter your new password twice.';
});

newPasswordForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const data = new FormData(newPasswordForm);
  if(data.get('password') !== data.get('confirm_password')){
    alert("Passwords don't match.");
    return;
  }
  const { error } = await supabase.auth.updateUser({ password: String(data.get('password')) });
  if(error){
    alert(friendlyError(error));
    return;
  }
  alert('Password updated. You are now logged in.');
  window.location.href = 'bookings.php';
});
</script>

</body>
</html>
