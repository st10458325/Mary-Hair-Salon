<?php
/**
 * components/booking-modal.php
 * ---------------------------------------------------------------
 * Full booking form as a modal overlay — personal info, service
 * details, booking type, a live calendar + time slots, notes,
 * and a running summary. Layout mirrors the reference design;
 * only the color theme changed (orange/glass instead of pink).
 *
 * Include it ONCE per page, then open it from anywhere:
 *   window.openBookingModal({ category: 'braids', title: 'Box Braids', price: 'From R450' });
 *
 * The Service/Style dropdowns use a small static catalog for now
 * (no DB yet) — once services live in a database, swap the
 * <option> lists below for a real query; nothing else needs to
 * change. Calendar availability (unavailableDays) is demo data
 * for the same reason.
 * ---------------------------------------------------------------
 */
?>
<div class="booking-modal" id="bookingModal" aria-hidden="true">
  <div class="booking-modal__backdrop" data-modal-close></div>

  <div class="booking-modal__panel glass" role="dialog" aria-modal="true" aria-labelledby="bookingModalTitle">
    <button type="button" class="booking-modal__close" data-modal-close aria-label="Close booking form">
      <svg width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M4 4l10 10M14 4L4 14" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>
    </button>

    <div class="bm-header">
      <h2 id="bookingModalTitle" class="bm-header__title">
        Book Your Appointment
        <svg width="26" height="26" viewBox="0 0 20 20" fill="none"><rect x="2.5" y="3.5" width="15" height="13.5" rx="1.8" stroke="currentColor" stroke-width="1.4"/><path d="M2.5 7.5H17.5" stroke="currentColor" stroke-width="1.4"/><path d="M6 2v3M14 2v3" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/><path d="M6.5 11l2 2 4-4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </h2>
      <div class="bm-header__underline"></div>
      <p class="bm-header__sub" id="bookingModalService">Fill in your details below to secure your booking.</p>
    </div>

    <form class="bm-form" id="bookingForm">

      <!-- 1. Personal Information -->
      <div class="bm-section-title"><span class="bm-num">1.</span> Personal Information</div>
      <div class="bm-row bm-row--2">
        <label class="bm-field">
          <span class="bm-field__icon"><svg width="15" height="15" viewBox="0 0 15 15" fill="none"><circle cx="7.5" cy="4.6" r="2.6" stroke="currentColor" stroke-width="1.2"/><path d="M2.3 13c.9-2.6 2.9-4 5.2-4s4.3 1.4 5.2 4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg></span>
          <span class="bm-field__body">
            <span class="bm-field__label">Name</span>
            <input type="text" name="name" placeholder="Enter your name" required>
          </span>
        </label>
        <label class="bm-field">
          <span class="bm-field__icon"><svg width="15" height="15" viewBox="0 0 15 15" fill="none"><circle cx="7.5" cy="4.6" r="2.6" stroke="currentColor" stroke-width="1.2"/><path d="M2.3 13c.9-2.6 2.9-4 5.2-4s4.3 1.4 5.2 4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg></span>
          <span class="bm-field__body">
            <span class="bm-field__label">Surname</span>
            <input type="text" name="surname" placeholder="Enter your surname" required>
          </span>
        </label>
      </div>

      <label class="bm-field">
        <span class="bm-field__icon"><svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M3 2.3c0 6.4 4.3 10.4 9.7 10.4l1.8-2.7-3.7-1.8-1.1 1.5a8 8 0 01-3.6-3.6l1.5-1.1L5.8 1 3 2.3z" stroke="currentColor" stroke-width="1.1" stroke-linejoin="round"/></svg></span>
        <span class="bm-field__body">
          <span class="bm-field__label">Contact (Cell No. / WhatsApp)</span>
          <input type="tel" name="contact" placeholder="e.g. 081 234 5678" required>
        </span>
      </label>

      <!-- 2. Service Details -->
      <div class="bm-section-title"><span class="bm-num">2.</span> Service Details</div>
      <div class="bm-row bm-row--3">
        <label class="bm-field bm-field--select">
          <span class="bm-field__icon"><svg width="15" height="15" viewBox="0 0 15 15" fill="none"><circle cx="3.2" cy="3.2" r="1.7" stroke="currentColor" stroke-width="1.1"/><circle cx="3.2" cy="11.8" r="1.7" stroke="currentColor" stroke-width="1.1"/><path d="M4.5 4.3L13 11.5M4.5 10.7L13 3.5" stroke="currentColor" stroke-width="1.1" stroke-linecap="round"/></svg></span>
          <span class="bm-field__body">
            <span class="bm-field__label">Service</span>
              <select name="service" id="bmService">
                <option value="" selected disabled>Loading services…</option>
              </select>
          </span>
          <svg class="bm-chev" width="11" height="11" viewBox="0 0 11 11" fill="none"><path d="M2 4l3.5 3.5L9 4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </label>

        <label class="bm-field bm-field--select">
          <span class="bm-field__icon"><svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M7.5 1.5L9 5l3.5.5-2.5 2.5.6 3.5L7.5 10 4.4 11.5l.6-3.5L2.5 5.5 6 5z" stroke="currentColor" stroke-width="1.1" stroke-linejoin="round"/></svg></span>
          <span class="bm-field__body">
            <span class="bm-field__label">Style</span>
            <select name="style" id="bmStyle" disabled>
              <option value="" selected disabled>Select a style</option>
            </select>
          </span>
          <svg class="bm-chev" width="11" height="11" viewBox="0 0 11 11" fill="none"><path d="M2 4l3.5 3.5L9 4" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </label>

        <label class="bm-field">
          <span class="bm-field__icon"><svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M2 7.6V3.5C2 2.7 2.7 2 3.5 2h4.1c.4 0 .8.2 1.1.4l5 5c.6.6.6 1.5 0 2.1l-3.6 3.6c-.6.6-1.5.6-2.1 0l-5-5C2.2 7.8 2 7.4 2 7z" stroke="currentColor" stroke-width="1.1" stroke-linejoin="round"/><circle cx="5.2" cy="5.2" r="0.8" fill="currentColor"/></svg></span>
          <span class="bm-field__body">
            <span class="bm-field__label">Cost (ZAR)</span>
            <input type="number" name="cost" id="bmCost" placeholder="e.g. 350" min="0">
          </span>
        </label>
      </div>

      <!-- 3. Booking Type -->
      <div class="bm-section-title"><span class="bm-num">3.</span> Booking Type</div>
      <div class="bm-type-row">
        <div class="bm-type-option" data-type="Incall" id="bmIncall">
          <span class="bm-radio"></span>
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none" class="bm-type-icon"><path d="M2 9l7-6 7 6M4 8v7h10V8" stroke="currentColor" stroke-width="1.3" stroke-linejoin="round"/></svg>
          <span class="bm-type-label">
            <strong>Incall</strong>
            <span>You come to me</span>
          </span>
        </div>
        <div class="bm-type-option is-active" data-type="Outcall" id="bmOutcall">
          <span class="bm-radio"></span>
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none" class="bm-type-icon"><path d="M2.5 11.5l1-4a1.6 1.6 0 011.5-1h6l1.5 1 3 1.5v2.5M2.5 11.5h13M2.5 11.5v1.7h2M13.5 13.2h2v-1.7" stroke="currentColor" stroke-width="1.2" stroke-linejoin="round"/><circle cx="5.5" cy="13.2" r="1.2" stroke="currentColor" stroke-width="1.1"/><circle cx="12.5" cy="13.2" r="1.2" stroke="currentColor" stroke-width="1.1"/></svg>
          <span class="bm-type-label">
            <strong>Outcall</strong>
            <span>I come to you</span>
          </span>
        </div>
      </div>

      <label class="bm-field bm-field--accent" id="bmLocationField">
        <span class="bm-field__icon"><svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M7.5 1.5c-2.7 0-4.5 2-4.5 4.6 0 3.2 4.5 7.2 4.5 7.2s4.5-4 4.5-7.2c0-2.6-1.8-4.6-4.5-4.6z" stroke="currentColor" stroke-width="1.2"/><circle cx="7.5" cy="6" r="1.5" stroke="currentColor" stroke-width="1.1"/></svg></span>
        <span class="bm-field__body">
          <span class="bm-field__label">Location (for Outcall)</span>
          <input type="text" name="location" id="bmLocationInput" placeholder="Enter the full address">
        </span>
      </label>

      <!-- 4 & 5. Date / Time -->
      <div class="bm-dt-row">
        <div>
          <div class="bm-section-title"><span class="bm-num">4.</span> Select Date</div>
          <div class="bm-calendar">
            <div class="bm-cal-head">
              <button type="button" id="bmPrevMonth" aria-label="Previous month"><svg width="12" height="12" viewBox="0 0 12 12" fill="none"><path d="M7.5 2.5L3.5 6l4 3.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg></button>
              <span id="bmCalMonthLabel">Month Year</span>
              <button type="button" id="bmNextMonth" aria-label="Next month"><svg width="12" height="12" viewBox="0 0 12 12" fill="none"><path d="M4.5 2.5l4 3.5-4 3.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/></svg></button>
            </div>
            <div class="bm-cal-grid" id="bmCalGrid"></div>
            <div class="bm-cal-legend">
              <span><i class="bm-dot bm-dot--green"></i>Available</span>
              <span><i class="bm-dot bm-dot--red"></i>Unavailable</span>
              <span><i class="bm-dot bm-dot--gray"></i>Past</span>
            </div>
            <div class="bm-cal-hint">Click on a green date to select</div>
          </div>
        </div>

        <div>
          <div class="bm-section-title"><span class="bm-num">5.</span> Select Time</div>
          <div class="bm-time-grid" id="bmTimeGrid"></div>
          <div class="bm-time-note">Time slots are based on the selected date.</div>
        </div>
      </div>

      <!-- 6. Additional Information -->
      <div class="bm-section-title"><span class="bm-num">6.</span> Additional Information</div>
      <div class="bm-row bm-row--2">
        <label class="bm-field bm-field--textarea">
          <span class="bm-field__icon"><svg width="15" height="15" viewBox="0 0 15 15" fill="none"><path d="M10.5 1.8l2.7 2.7-8 8-3.4.7.7-3.4z" stroke="currentColor" stroke-width="1.1" stroke-linejoin="round"/></svg></span>
          <span class="bm-field__body">
            <span class="bm-field__label">Notes / Special Requests</span>
            <textarea name="notes" id="bmNotes" placeholder="Any special requests or additional information..."></textarea>
          </span>
        </label>
        <label class="bm-field">
          <span class="bm-field__icon"><svg width="15" height="15" viewBox="0 0 15 15" fill="none"><circle cx="4" cy="4" r="1.8" stroke="currentColor" stroke-width="1.1"/><circle cx="11" cy="11" r="1.8" stroke="currentColor" stroke-width="1.1"/><path d="M11.5 2.5l-9 9" stroke="currentColor" stroke-width="1.1" stroke-linecap="round"/></svg></span>
          <span class="bm-field__body">
            <span class="bm-field__label">Applicable Discount (%)</span>
            <input type="number" name="discount" id="bmDiscount" placeholder="e.g. 10" min="0" max="100" value="0">
          </span>
        </label>
      </div>

      <!-- 7. Booking Summary -->
      <div class="bm-section-title"><span class="bm-num">7.</span> Booking Summary</div>
      <div class="bm-summary">
        <div class="bm-summary__list">
          <div class="bm-srow"><span class="bm-srow__label">Service</span><span class="bm-srow__val" id="bmSumService">-</span></div>
          <div class="bm-srow"><span class="bm-srow__label">Style</span><span class="bm-srow__val" id="bmSumStyle">-</span></div>
          <div class="bm-srow"><span class="bm-srow__label">Booking Type</span><span class="bm-srow__val" id="bmSumType">Outcall</span></div>
          <div class="bm-srow"><span class="bm-srow__label">Date</span><span class="bm-srow__val" id="bmSumDate">-</span></div>
          <div class="bm-srow"><span class="bm-srow__label">Time</span><span class="bm-srow__val" id="bmSumTime">-</span></div>
          <div class="bm-srow"><span class="bm-srow__label">Location</span><span class="bm-srow__val" id="bmSumLocation">-</span></div>
        </div>
        <div class="bm-summary__cost">
          <div class="bm-crow"><span>Cost (ZAR)</span><span id="bmSumCost">R0.00</span></div>
          <div class="bm-crow"><span>Discount</span><span id="bmSumDiscount">0%</span></div>
          <div class="bm-crow bm-crow--total"><span>Amount After Discount</span><span id="bmSumTotal">R0.00</span></div>
        </div>
      </div>

      <button type="submit" class="bm-submit">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="2" y="3" width="12" height="10.5" rx="1.4" stroke="currentColor" stroke-width="1.3"/><path d="M2 6.5h12" stroke="currentColor" stroke-width="1.3"/><path d="M5.2 10l1.8 1.8L10.8 8" stroke="currentColor" stroke-width="1.3" stroke-linecap="round" stroke-linejoin="round"/></svg>
        <span id="bmSubmitLabel">Confirm Booking</span>
      </button>
      <p class="bm-footnote">Your information is safe and will never be shared.</p>
    </form>
  </div>
</div>

<style>
  @import url('https://fonts.googleapis.com/css2?family=Alex+Brush&display=swap');

  .booking-modal{
    --bm-green: #3ecf6e;
    --bm-red: #e0475a;
    position: fixed;
    inset: 0;
    z-index: 500;
    display: none;
    align-items: flex-start;
    justify-content: center;
    padding: 40px 20px;
    overflow-y: auto;
  }
  .booking-modal.is-open{ display: flex; }

  .booking-modal__backdrop{
    position: fixed;
    inset: 0;
    background: rgba(6, 4, 3, 0.72);
    backdrop-filter: blur(4px);
  }

  .booking-modal__panel{
    position: relative;
    z-index: 1;
    width: 100%;
    max-width: 900px;
    border-radius: var(--radius-lg);
    background: var(--bg-elevated);
    border: 1px solid var(--glass-border);
    padding: 34px 36px 30px;
    animation: bm-rise .28s ease;
    margin-bottom: 40px;
  }
  @keyframes bm-rise{
    from{ opacity: 0; transform: translateY(16px) scale(0.98); }
    to{ opacity: 1; transform: translateY(0) scale(1); }
  }

  .booking-modal__close{
    position: absolute;
    top: 18px; right: 18px;
    width: 34px; height: 34px;
    display: flex; align-items: center; justify-content: center;
    border-radius: 50%;
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    color: var(--text-dim);
    z-index: 2;
  }
  .booking-modal__close:hover{ color: var(--orange-light); border-color: var(--orange); }

  /* ---- Header ---- */
  .bm-header{ text-align: center; margin-bottom: 30px; }
  .bm-header__title{
    font-family: 'Alex Brush', cursive;
    font-weight: 400;
    font-size: 2.6rem;
    display: inline-flex;
    align-items: center;
    gap: 12px;
    color: #fff;
  }
  .bm-header__title svg{ color: var(--orange); }
  .bm-header__underline{
    width: 56px; height: 2px;
    background: var(--orange);
    margin: 8px auto 8px;
  }
  .bm-header__sub{ color: var(--text-dim); font-size: 0.92rem; }

  /* ---- Sections / fields ---- */
  .bm-form{ display: flex; flex-direction: column; }
  .bm-section-title{
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 26px 0 12px;
    font-size: 1rem;
    font-weight: 600;
  }
  .bm-section-title:first-child{ margin-top: 0; }
  .bm-num{ color: var(--orange-light); font-weight: 700; }

  .bm-row{ display: grid; gap: 14px; }
  .bm-row--2{ grid-template-columns: 1fr 1fr; }
  .bm-row--3{ grid-template-columns: 1fr 1fr 1fr; }

  .bm-field{
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    border-radius: var(--radius-sm);
    padding: 10px 14px;
    display: flex;
    align-items: center;
    gap: 11px;
    position: relative;
    transition: border-color .15s ease;
  }
  .bm-field:focus-within{ border-color: var(--orange); }
  .bm-field--accent{ border-color: var(--orange-glow); }
  .bm-field--textarea{ align-items: flex-start; }

  .bm-field__icon{ color: var(--orange-light); flex-shrink: 0; margin-top: 1px; }
  .bm-field--textarea .bm-field__icon{ margin-top: 4px; }
  .bm-field__body{ display: flex; flex-direction: column; width: 100%; gap: 2px; }
  .bm-field__label{ font-size: 0.78rem; color: var(--text-dim); }

  .bm-field input, .bm-field select, .bm-field textarea{
    background: transparent;
    border: none;
    outline: none;
    color: var(--text);
    font-family: inherit;
    font-size: 0.92rem;
    padding: 0;
    width: 100%;
    appearance: none;
  }
  .bm-field textarea{ resize: vertical; min-height: 66px; }
  .bm-field select option{ background: var(--bg-elevated); color: var(--text); }
  .bm-field--select{ padding-right: 30px; }
  .bm-chev{ position: absolute; right: 12px; top: 50%; transform: translateY(-50%); color: var(--text-faint); pointer-events: none; }

  /* ---- Booking type cards ---- */
  .bm-type-row{ display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 14px; }
  .bm-type-option{
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    border-radius: var(--radius-sm);
    padding: 13px 15px;
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: border-color .15s ease, background .15s ease;
  }
  .bm-type-option.is-active{ border-color: var(--orange); background: var(--orange-soft); }
  .bm-radio{
    width: 17px; height: 17px;
    border-radius: 50%;
    border: 2px solid var(--text-faint);
    flex-shrink: 0;
    display: flex; align-items: center; justify-content: center;
  }
  .bm-type-option.is-active .bm-radio{ border-color: var(--orange); }
  .bm-type-option.is-active .bm-radio::after{
    content: ''; width: 7px; height: 7px; border-radius: 50%; background: var(--orange);
  }
  .bm-type-icon{ color: var(--orange-light); flex-shrink: 0; }
  .bm-type-label strong{ display: block; font-size: 0.9rem; font-weight: 600; }
  .bm-type-label span{ display: block; font-size: 0.76rem; color: var(--text-dim); margin-top: 1px; }

  /* ---- Date / Time ---- */
  .bm-dt-row{ display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-top: 4px; }

  .bm-calendar{ background: var(--glass-bg); border: 1px solid var(--glass-border); border-radius: var(--radius-sm); padding: 15px; }
  .bm-cal-head{ display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; font-weight: 600; font-size: 0.9rem; }
  .bm-cal-head button{ background: none; border: none; color: var(--text); cursor: pointer; padding: 4px 8px; }
  .bm-cal-head button:hover{ color: var(--orange-light); }
  .bm-cal-grid{ display: grid; grid-template-columns: repeat(7, 1fr); gap: 5px; text-align: center; }
  .bm-cal-grid .bm-dow{ color: var(--text-faint); font-size: 0.7rem; padding-bottom: 5px; }
  .bm-cal-day{
    aspect-ratio: 1;
    display: flex; align-items: center; justify-content: center;
    border-radius: 7px;
    font-size: 0.8rem;
    color: var(--text-dim);
  }
  .bm-cal-day.other{ color: #3a3632; }
  .bm-cal-day.past{ color: #55504a; }
  .bm-cal-day.available{ background: rgba(62,207,110,0.10); color: var(--bm-green); cursor: pointer; }
  .bm-cal-day.available:hover{ background: rgba(62,207,110,0.2); }
  .bm-cal-day.unavailable{ color: var(--bm-red); }
  .bm-cal-day.selected{ background: var(--orange); color: #fff; font-weight: 700; }
  .bm-cal-legend{ display: flex; justify-content: center; gap: 14px; margin-top: 12px; font-size: 0.7rem; color: var(--text-faint); }
  .bm-cal-legend span{ display: flex; align-items: center; gap: 5px; }
  .bm-dot{ width: 7px; height: 7px; border-radius: 50%; display: inline-block; }
  .bm-dot--green{ background: var(--bm-green); }
  .bm-dot--red{ background: var(--bm-red); }
  .bm-dot--gray{ background: #5a565c; }
  .bm-cal-hint{ text-align: center; font-size: 0.7rem; color: var(--text-faint); margin-top: 8px; }

  .bm-time-grid{ display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; }
  .bm-time-slot{
    background: var(--glass-bg);
    border: 1px solid var(--glass-border);
    border-radius: var(--radius-sm);
    padding: 12px 4px;
    text-align: center;
    font-weight: 600;
    font-size: 0.85rem;
    cursor: pointer;
    transition: all .15s ease;
  }
  .bm-time-slot:hover{ border-color: var(--orange-glow); }
  .bm-time-slot.selected{ background: var(--orange); border-color: var(--orange); color: #fff; }
  .bm-time-note{
    margin-top: 14px;
    background: var(--orange-soft);
    border: 1px solid var(--orange-glow);
    color: var(--orange-light);
    border-radius: var(--radius-sm);
    padding: 10px 13px;
    font-size: 0.78rem;
  }

  /* ---- Summary ---- */
  .bm-summary{
    border: 1px solid var(--orange-glow);
    border-radius: var(--radius-sm);
    padding: 18px 20px;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 18px;
  }
  .bm-summary__list{ display: flex; flex-direction: column; gap: 9px; }
  .bm-srow{ display: flex; justify-content: space-between; gap: 10px; font-size: 0.85rem; color: var(--text-dim); }
  .bm-srow__val{ color: var(--text); text-align: right; }
  .bm-summary__cost{ display: flex; flex-direction: column; gap: 9px; justify-content: center; }
  .bm-crow{ display: flex; justify-content: space-between; font-size: 0.85rem; color: var(--text-dim); }
  .bm-crow--total{ font-size: 1.15rem; font-weight: 700; color: var(--orange-light); }

  /* ---- Submit ---- */
  .bm-submit{
    width: 100%;
    margin-top: 22px;
    background: linear-gradient(135deg, var(--orange-light), var(--orange));
    border: none;
    color: #fff;
    font-size: 0.98rem;
    font-weight: 700;
    padding: 15px;
    border-radius: var(--radius-sm);
    cursor: pointer;
    display: flex; align-items: center; justify-content: center; gap: 9px;
    transition: opacity .15s ease;
  }
  .bm-submit:hover{ opacity: .92; }
  .bm-footnote{ text-align: center; color: var(--text-faint); font-size: 0.76rem; margin-top: 12px; }

  @media (max-width: 720px){
    .booking-modal__panel{ padding: 26px 20px 22px; }
    .bm-row--2, .bm-row--3, .bm-type-row, .bm-dt-row, .bm-summary{ grid-template-columns: 1fr; }
    .bm-header__title{ font-size: 2rem; }
    .bm-time-grid{ grid-template-columns: repeat(3, 1fr); }
  }
</style>

<script type="module">
  import { auth, db } from '/assets/js/firebase-config.js';
  import { addDoc, collection, serverTimestamp, getDocs, updateDoc, doc } from "https://www.gstatic.com/firebasejs/12.17.0/firebase-firestore.js";

(function(){
  const modal = document.getElementById('bookingModal');
  const form = document.getElementById('bookingForm');
  let lastFocused = null;

  let catalogReady;
  let servicesByCategory = {};

  const serviceSelect = document.getElementById('bmService');
  const styleSelect = document.getElementById('bmStyle');

  function setSelectValue(select, value){
    if(value == null || value === '') return false;
    const option = Array.from(select.options).find(item => item.value === value);
    if(!option) return false;
    select.value = value;
    return true;
  }

  function initCatalog(){
    catalogReady = (async () => {
      try {
        const [catSnap, svcSnap] = await Promise.all([
          getDocs(collection(db, 'categories')),
          getDocs(collection(db, 'services')),
        ]);

        serviceSelect.innerHTML = '<option value="" selected disabled>Select a service</option>';
        catSnap.forEach(d => {
          const cat = d.data();
          const option = document.createElement('option');
          option.value = d.id;
          option.textContent = cat.label || d.id;
          serviceSelect.appendChild(option);
        });

        servicesByCategory = {};
        svcSnap.forEach(d => {
          const svc = d.data();
          if(!servicesByCategory[svc.category]) servicesByCategory[svc.category] = [];
          servicesByCategory[svc.category].push(svc.title);
        });
      } catch(err){
        console.error('Failed to load service catalog', err);
      }
    })();
  }

  function populateStyles(category, preferredStyle){
    styleSelect.innerHTML = '<option value="" selected disabled>Select a style</option>';
    const styles = servicesByCategory[category] || [];
    styles.forEach(style => {
      const option = document.createElement('option');
      option.value = style;
      option.textContent = style;
      styleSelect.appendChild(option);
    });
    styleSelect.disabled = styles.length === 0;
    if(preferredStyle) setSelectValue(styleSelect, preferredStyle);
  }

  /* ---------------- Booking type ---------------- */
  const incallOption = document.getElementById('bmIncall');
  const outcallOption = document.getElementById('bmOutcall');
  const locationField = document.getElementById('bmLocationField');
  let bookingType = 'Outcall';

  function setBookingType(type){
    bookingType = type;
    incallOption.classList.toggle('is-active', type === 'Incall');
    outcallOption.classList.toggle('is-active', type === 'Outcall');
    locationField.style.display = (type === 'Outcall') ? 'flex' : 'none';
    updateSummary();
  }
  incallOption.addEventListener('click', () => setBookingType('Incall'));
  outcallOption.addEventListener('click', () => setBookingType('Outcall'));

  /* ---------------- Calendar ---------------- */
  const calGrid = document.getElementById('bmCalGrid');
  const calMonthLabel = document.getElementById('bmCalMonthLabel');
  const monthNames = ["January","February","March","April","May","June","July","August","September","October","November","December"];
  const dows = ['Mon','Tue','Wed','Thu','Fri','Sat','Sun'];
  const today = new Date();
  today.setHours(0,0,0,0);

  const calState = {
    viewYear: today.getFullYear(),
    viewMonth: today.getMonth(),
    selectedDate: null,
    // demo unavailable days (day-of-month numbers) — replace with real availability data later
    unavailableDays: [10, 11, 17, 18, 24, 25],
  };

  function pad(n){ return n < 10 ? '0'+n : ''+n; }

  function renderCalendar(){
    calGrid.innerHTML = '';
    dows.forEach(d => {
      const el = document.createElement('div');
      el.className = 'bm-dow';
      el.textContent = d;
      calGrid.appendChild(el);
    });

    const year = calState.viewYear;
    const month = calState.viewMonth;
    calMonthLabel.textContent = `${monthNames[month]} ${year}`;

    const firstOfMonth = new Date(year, month, 1);
    const startOffset = (firstOfMonth.getDay() + 6) % 7;
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const daysInPrevMonth = new Date(year, month, 0).getDate();

    const cells = [];
    for(let i = startOffset; i > 0; i--) cells.push({ day: daysInPrevMonth - i + 1, type: 'other' });
    for(let d = 1; d <= daysInMonth; d++) cells.push({ day: d, type: 'current' });
    const remainder = cells.length % 7;
    if(remainder !== 0){
      const trailing = 7 - remainder;
      for(let d = 1; d <= trailing; d++) cells.push({ day: d, type: 'other' });
    }

    cells.forEach(cell => {
      const el = document.createElement('div');
      el.className = 'bm-cal-day';

      if(cell.type === 'other'){
        el.classList.add('other');
        el.textContent = cell.day;
        calGrid.appendChild(el);
        return;
      }

      const cellDate = new Date(year, month, cell.day);
      cellDate.setHours(0,0,0,0);
      const dateStr = `${year}-${pad(month+1)}-${pad(cell.day)}`;
      el.textContent = cell.day;

      if(cellDate < today){
        el.classList.add('past');
      } else if(calState.unavailableDays.includes(cell.day)){
        el.classList.add('unavailable');
      } else {
        el.classList.add('available');
        el.addEventListener('click', () => selectDate(dateStr, el));
      }

      if(calState.selectedDate === dateStr) el.classList.add('selected');
      calGrid.appendChild(el);
    });
  }

  function selectDate(dateStr, el){
    calState.selectedDate = dateStr;
    document.querySelectorAll('.bm-cal-day.selected').forEach(n => n.classList.remove('selected'));
    el.classList.add('selected');
    updateSummary();
  }

  document.getElementById('bmPrevMonth').addEventListener('click', () => {
    calState.viewMonth--;
    if(calState.viewMonth < 0){ calState.viewMonth = 11; calState.viewYear--; }
    renderCalendar();
  });
  document.getElementById('bmNextMonth').addEventListener('click', () => {
    calState.viewMonth++;
    if(calState.viewMonth > 11){ calState.viewMonth = 0; calState.viewYear++; }
    renderCalendar();
  });

  function formatSelectedDate(){
    if(!calState.selectedDate) return '-';
    const [y, m, d] = calState.selectedDate.split('-').map(Number);
    return `${d} ${monthNames[m-1].slice(0,3)} ${y}`;
  }

  /* ---------------- Time slots ---------------- */
  const timeGrid = document.getElementById('bmTimeGrid');
  const times = ['08:00','09:00','10:00','11:00','12:00','13:00','14:00','15:00','16:00','17:00','18:00','19:00'];
  let selectedTime = null;

  function renderTimeSlots(){
    timeGrid.innerHTML = '';
    times.forEach(t => {
      const el = document.createElement('div');
      el.className = 'bm-time-slot';
      if(t === selectedTime) el.classList.add('selected');
      el.textContent = t;
      el.addEventListener('click', () => {
        selectedTime = t;
        document.querySelectorAll('.bm-time-slot.selected').forEach(n => n.classList.remove('selected'));
        el.classList.add('selected');
        updateSummary();
      });
      timeGrid.appendChild(el);
    });
  }

  /* ---------------- Summary ---------------- */
  function formatZAR(n){ return 'R' + n.toFixed(2); }

  function selectedText(select){
    const option = select.selectedOptions && select.selectedOptions[0];
    return option ? option.textContent : '-';
  }

  function updateSummary(){
    const cost = parseFloat(document.getElementById('bmCost').value) || 0;
    const discount = parseFloat(document.getElementById('bmDiscount').value) || 0;
    const location = document.getElementById('bmLocationInput').value || '-';

    document.getElementById('bmSumService').textContent = selectedText(serviceSelect);
    document.getElementById('bmSumStyle').textContent = selectedText(styleSelect);
    document.getElementById('bmSumType').textContent = bookingType;
    document.getElementById('bmSumDate').textContent = formatSelectedDate();
    document.getElementById('bmSumTime').textContent = selectedTime || '-';
    document.getElementById('bmSumLocation').textContent = bookingType === 'Outcall' ? location : 'N/A';

    document.getElementById('bmSumCost').textContent = formatZAR(cost);
    document.getElementById('bmSumDiscount').textContent = discount + '%';
    document.getElementById('bmSumTotal').textContent = formatZAR(cost - (cost * discount / 100));
  }

  ['bmService','bmStyle','bmCost','bmDiscount','bmLocationInput'].forEach(id => {
    const el = document.getElementById(id);
    el.addEventListener('input', updateSummary);
    el.addEventListener('change', updateSummary);
  });

  serviceSelect.addEventListener('change', () => {
    populateStyles(serviceSelect.value, '');
    updateSummary();
  });

/* ---------------- Prefill from a clicked service card, or an existing booking for reschedule ---------------- */
  let currentEditId = null;

  window.openBookingModal = async function(service, bookingRecord = null){
    if(!auth.currentUser){
      if(typeof window.openAuthModal === 'function'){
        window.openAuthModal('login');
      } else {
        window.location.href = 'login.php';
      }
      return;
    }
    await catalogReady;

    currentEditId = bookingRecord ? bookingRecord.id : null;
    document.getElementById('bmSubmitLabel').textContent = bookingRecord ? 'Save Changes' : 'Confirm Booking';

    if(bookingRecord){
      // ---- Reschedule: full prefill from an existing booking doc ----
      form.querySelector('input[name="name"]').value = bookingRecord.name || '';
      form.querySelector('input[name="surname"]').value = bookingRecord.surname || '';
      form.querySelector('input[name="contact"]').value = bookingRecord.contact || '';
      document.getElementById('bmNotes').value = bookingRecord.notes || '';
      document.getElementById('bmCost').value = bookingRecord.cost ?? '';
      document.getElementById('bmDiscount').value = bookingRecord.discount ?? 0;
      document.getElementById('bmLocationInput').value = bookingRecord.location || '';

      setSelectValue(serviceSelect, bookingRecord.category);
      populateStyles(bookingRecord.category, bookingRecord.service);

      setBookingType(bookingRecord.type || 'Outcall');

      if(bookingRecord.date){
        const [y, m] = bookingRecord.date.split('-').map(Number);
        calState.viewYear = y;
        calState.viewMonth = m - 1;
        calState.selectedDate = bookingRecord.date;
      }
      selectedTime = bookingRecord.time || null;
      renderCalendar();
      renderTimeSlots();

      document.getElementById('bookingModalService').textContent =
        `Rescheduling: ${selectedText(serviceSelect)} / ${bookingRecord.service || ''}`;
    } else {
      // ---- New booking from a service card (unchanged behaviour) ----
      service = service || {};
      const costInput = document.getElementById('bmCost');

      if(service.category){
        const categorySet = setSelectValue(serviceSelect, service.category);
        populateStyles(service.category, service.title || '');
        if(categorySet && service.title){
          setSelectValue(styleSelect, service.title);
        }
        document.getElementById('bookingModalService').textContent = service.title
          ? `Booking: ${service.categoryLabel || selectedText(serviceSelect)} / ${service.title}`
          : `Booking: ${service.categoryLabel || selectedText(serviceSelect)}`;
      } else {
        document.getElementById('bookingModalService').textContent = 'Fill in your details below to secure your booking.';
      }

      if(service.price){
        const numeric = (service.price.match(/\d+/) || [''])[0];
        if(numeric) costInput.value = numeric;
      }

      if(!service.category){
        populateStyles('', '');
        serviceSelect.value = '';
      }
    }

    updateSummary();
    lastFocused = document.activeElement;
    modal.classList.add('is-open');
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
    form.querySelector('input[name="name"]').focus();
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

  form.addEventListener('submit', async function(e){
      e.preventDefault();
      if(!calState.selectedDate){ alert('Please select a date.'); return; }
      if(!selectedTime){ alert('Please select a time.'); return; }
      if(!serviceSelect.value){ alert('Please select a service category.'); return; }
      if(!styleSelect.value){ alert('Please select a style.'); return; }
      if(!auth.currentUser){
        alert('Please log in to book an appointment.');
        closeModal();
        if(typeof window.openAuthModal === 'function') window.openAuthModal('login');
        return;
      }

      const submitBtn = form.querySelector('.bm-submit');
      submitBtn.disabled = true;

      const cost = parseFloat(document.getElementById('bmCost').value) || 0;
      const discount = parseFloat(document.getElementById('bmDiscount').value) || 0;
      const total = cost - (cost * discount / 100);

      const bookingData = {
        name: form.querySelector('input[name="name"]').value,
        surname: form.querySelector('input[name="surname"]').value,
        contact: form.querySelector('input[name="contact"]').value,
        category: serviceSelect.value,
        categoryLabel: selectedText(serviceSelect),
        service: selectedText(styleSelect),
        type: bookingType,
        location: bookingType === 'Outcall' ? document.getElementById('bmLocationInput').value : '',
        date: calState.selectedDate,
        dateFormatted: formatSelectedDate(),
        time: selectedTime,
        notes: document.getElementById('bmNotes').value,
        cost,
        discount,
        total,
        price: total.toFixed(2),
        status: 'pending',
      };

      try {
        if(currentEditId){
          await updateDoc(doc(db, 'bookings', currentEditId), {
            ...bookingData,
            updatedAt: serverTimestamp(),
          });
          alert('Booking updated! Your new date/time is pending confirmation.');
        } else {
          await addDoc(collection(db, 'bookings'), {
            ...bookingData,
            uid: auth.currentUser.uid,
            createdAt: serverTimestamp(),
          });
          alert('Booking confirmed!');
        }
        closeModal();
        form.reset();
        setBookingType('Outcall');
        calState.selectedDate = null;
        selectedTime = null;
        currentEditId = null;
        populateStyles('', '');
        renderCalendar();
        renderTimeSlots();
        updateSummary();
      } catch(err){
        console.error(err);
        alert('Something went wrong saving your booking. Please try again.');
      } finally {
        submitBtn.disabled = false;
      }
  });
  /* ---------------- Init ---------------- */
  initCatalog();
  populateStyles('', '');
  setBookingType('Outcall');
  renderCalendar();
  renderTimeSlots();
  updateSummary();
})();
</script>
