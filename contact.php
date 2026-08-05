<?php
/**
 * contact.php — Mary Hair salon
 * ---------------------------------------------------------------
 * The contact form has no backend yet (matches the pattern used
 * by booking-modal.php and auth-form.php) — it shows a placeholder
 * confirmation on submit.
 * ---------------------------------------------------------------
 */
require_once __DIR__ . '/components/hero.php';
?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Contact — Mary Hair salon</title>
<link rel="stylesheet" href="style.css">
</head>
<body>

<?php include __DIR__ . '/components/navigation.php'; ?>

<?php render_hero([
    'eyebrow' => 'Get In Touch',
    'title'   => 'Contact Us',
    'text'    => 'Questions about a style, or want to check availability before you book? Send a message or reach out directly.',
    'compact' => true,
]); ?>

<section class="contact-section">
  <div class="contact-section__inner">

    <form class="contact-form glass" id="contactForm">
      <h2>Send a Message</h2>

      <label class="contact-form__field">
        <span>Name</span>
        <input type="text" name="name" placeholder="Enter your name" required>
      </label>

      <label class="contact-form__field">
        <span>Email</span>
        <input type="email" name="email" placeholder="you@example.com" required>
      </label>

      <label class="contact-form__field">
        <span>Message</span>
        <textarea name="message" placeholder="How can we help?" required></textarea>
      </label>

      <button type="submit" class="contact-form__submit">Send Message</button>
    </form>

    <div class="contact-info">
      <div class="contact-info__card glass">
        <span class="contact-info__icon"><svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M9 1.5c-3.3 0-5.6 2.5-5.6 5.7C3.4 11.2 9 16.5 9 16.5s5.6-5.3 5.6-9.3c0-3.2-2.3-5.7-5.6-5.7z" stroke="currentColor" stroke-width="1.3"/><circle cx="9" cy="7.3" r="2" stroke="currentColor" stroke-width="1.3"/></svg></span>
        <div>
          <h3>Studio Address</h3>
          <p>14 Bree Street, Cape Town, 8001</p>
        </div>
      </div>

      <div class="contact-info__card glass">
        <span class="contact-info__icon"><svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M3 4.5c0 6 4.5 10.5 10.5 10.5l2-3-4-2-1.2 1.6a8.6 8.6 0 01-3.9-3.9L8 5.5l-2-4-3 2z" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/></svg></span>
        <div>
          <h3>WhatsApp / Call</h3>
          <p>081 234 5678</p>
        </div>
      </div>

      <div class="contact-info__card glass">
        <span class="contact-info__icon"><svg width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="7" stroke="currentColor" stroke-width="1.3"/><path d="M9 5v4l2.5 1.5" stroke="currentColor" stroke-width="1.3" stroke-linecap="round"/></svg></span>
        <div>
          <h3>Opening Hours</h3>
          <p>Tue – Sat: 09:00 – 18:00<br>Sun – Mon: Closed</p>
        </div>
      </div>
    </div>

  </div>
</section>

<?php include __DIR__ . '/components/booking-modal.php'; ?>
<?php include __DIR__ . '/components/auth-modal.php'; ?>
<?php include __DIR__ . '/components/footer.php'; ?>

<style>
  .contact-section{ padding: 20px 40px 90px; }
  .contact-section__inner{
    max-width: 1100px;
    margin: 0 auto;
    display: grid;
    grid-template-columns: 1.3fr 1fr;
    gap: 24px;
    align-items: start;
  }

  .contact-form{
    border-radius: var(--radius-lg);
    padding: 30px 28px;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  .contact-form h2{
    font-family: var(--font-display);
    font-size: 22px;
    font-weight: 600;
    margin-bottom: 4px;
  }
  .contact-form__field{ display: flex; flex-direction: column; gap: 6px; }
  .contact-form__field span{ font-size: 12px; color: var(--text-dim); font-weight: 600; }
  .contact-form__field input, .contact-form__field textarea{
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    border-radius: var(--radius-sm);
    padding: 12px 14px;
    font-size: 14px;
    outline: none;
    transition: border-color .15s ease;
  }
  .contact-form__field input:focus, .contact-form__field textarea:focus{ border-color: var(--orange); }
  .contact-form__field textarea{ min-height: 130px; resize: vertical; }
  .contact-form__submit{
    padding: 14px;
    border-radius: var(--radius-sm);
    border: none;
    background: linear-gradient(135deg, var(--orange-light), var(--orange));
    color: #fff;
    font-size: 15px;
    font-weight: 700;
    transition: opacity .15s ease;
  }
  .contact-form__submit:hover{ opacity: 0.92; }

  .contact-info{ display: flex; flex-direction: column; gap: 16px; }
  .contact-info__card{
    display: flex;
    gap: 14px;
    padding: 20px;
    border-radius: var(--radius-md);
  }
  .contact-info__icon{
    width: 38px; height: 38px;
    display: flex; align-items: center; justify-content: center;
    border-radius: 50%;
    background: var(--orange-soft);
    color: var(--orange-light);
    flex-shrink: 0;
  }
  .contact-info__card h3{ font-size: 14px; font-weight: 700; margin-bottom: 4px; }
  .contact-info__card p{ font-size: 13px; color: var(--text-dim); line-height: 1.55; }

  @media (max-width: 860px){
    .contact-section__inner{ grid-template-columns: 1fr; }
  }
  @media (max-width: 640px){
    .contact-section{ padding: 10px 20px 70px; }
  }
</style>

<script type="module">
  import { db } from '/assets/js/firebase-config.js';
  import { addDoc, collection, serverTimestamp } from "https://www.gstatic.com/firebasejs/12.17.0/firebase-firestore.js";

  document.getElementById('contactForm').addEventListener('submit', async function(e){
    e.preventDefault();
    const form = this;
    const data = new FormData(form);
    const submitBtn = form.querySelector('.contact-form__submit');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Sending…';

    try {
      await addDoc(collection(db, 'messages'), {
        name: data.get('name'),
        email: data.get('email'),
        message: data.get('message'),
        createdAt: serverTimestamp(),
      });
      alert("Message sent! We'll get back to you soon.");
      form.reset();
    } catch(err){
      console.error(err);
      alert('Something went wrong sending your message. Please try again.');
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Send Message';
    }
  });
</script>

</body>
</html>
