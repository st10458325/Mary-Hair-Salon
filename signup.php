<?php
/**
 * signup.php — Mary Hair salon
 * ---------------------------------------------------------------
 * Standalone page version of the signup form — mirrors login.php,
 * both reuse components/auth-form.php.
 * ---------------------------------------------------------------
 */
require_once __DIR__ . '/components/auth-form.php';
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Sign Up — Mary Hair salon</title>
<link rel="stylesheet" href="style.css">
</head>
<body>

<?php include __DIR__ . '/components/navigation.php'; ?>

<main class="auth-page">
  <div class="glow-field"><span></span><span></span><span></span></div>
  <div class="auth-page__panel glass">
    <div class="auth-page__head">
      <h1>Create Your Account</h1>
      <p>Book faster and keep track of your appointments.</p>
    </div>
    <?php render_auth_form('signup'); ?>
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
    max-width: 440px;
    border-radius: var(--radius-lg);
    padding: 36px 32px;
  }
  .auth-page__head{ text-align: center; margin-bottom: 26px; }
  .auth-page__head h1{
    font-family: var(--font-display);
    font-size: 26px;
    font-weight: 600;
    margin-bottom: 6px;
  }
  .auth-page__head p{ font-size: 13.5px; color: var(--text-dim); }
</style>

</body>
</html>
