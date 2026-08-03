<?php
/**
 * forgot-password.php — Mary Hair salon
 * ---------------------------------------------------------------
 * Linked from components/auth-form.php's login mode. No backend
 * yet — submitting shows a placeholder confirmation, same pattern
 * as every other form in the project until auth/DB exists.
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

    <form class="auth-form" id="forgotForm">
      <label class="auth-form__field">
        <span>Email</span>
        <input type="email" name="email" placeholder="you@example.com" required>
      </label>
      <button type="submit" class="auth-form__submit">Send Reset Link</button>
      <p class="auth-form__switch">
        Remembered it? <a href="login.php">Back to Log In</a>
      </p>
    </form>
  </div>
</main>

<?php include __DIR__ . '/components/footer.php'; ?>

<style>
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

<script>
document.getElementById('forgotForm').addEventListener('submit', function(e){
  e.preventDefault();
  alert('Reset link sent! (Connect this to your backend to actually send it.)');
  this.reset();
});
</script>

</body>
</html>
