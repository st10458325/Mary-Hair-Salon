/**
 * Seed data for Mary Hair Salon (simulated data, at least 10 rows per table).
 * Kept separate from seed.js so it can be reviewed in a PR and checked with --dry-run.
 * Column names match supabase/migrations/001_schema.sql.
 */

const DEMO_PASSWORD = 'MaryDemo#2026'; // demo accounts only — never reuse for real accounts

// ---------- users (13: 1 manager, 4 stylists, 8 customers) ----------
const users = [
  { key: 'seed-manager-mary', name: 'Mary', surname: 'Nkosi', email: 'manager@maryhairsalon.test', contact: '0821000001', role: 'manager', title: 'Owner & Manager' },

  { key: 'seed-stylist-thandi', name: 'Thandi', surname: 'Mokoena', email: 'thandi@maryhairsalon.test', contact: '0821000002', role: 'employee', title: 'Senior Braider' },
  { key: 'seed-stylist-lerato', name: 'Lerato', surname: 'Dlamini', email: 'lerato@maryhairsalon.test', contact: '0821000003', role: 'employee', title: 'Colour Specialist' },
  { key: 'seed-stylist-zara', name: 'Zara', surname: 'Mhlanga', email: 'zara@maryhairsalon.test', contact: '0821000004', role: 'employee', title: 'Precision Cuts' },
  { key: 'seed-stylist-sipho', name: 'Sipho', surname: 'Khumalo', email: 'sipho@maryhairsalon.test', contact: '0821000005', role: 'employee', title: 'Barber & Locs' },

  { key: 'seed-customer-01', name: 'Ayanda', surname: 'Zulu', email: 'ayanda@example.test', contact: '0731000001', role: 'customer' },
  { key: 'seed-customer-02', name: 'Naledi', surname: 'Sithole', email: 'naledi@example.test', contact: '0731000002', role: 'customer' },
  { key: 'seed-customer-03', name: 'Kagiso', surname: 'Molefe', email: 'kagiso@example.test', contact: '0731000003', role: 'customer' },
  { key: 'seed-customer-04', name: 'Precious', surname: 'Ndlovu', email: 'precious@example.test', contact: '0731000004', role: 'customer' },
  { key: 'seed-customer-05', name: 'Bongani', surname: 'Mthembu', email: 'bongani@example.test', contact: '0731000005', role: 'customer' },
  { key: 'seed-customer-06', name: 'Lindiwe', surname: 'Cele', email: 'lindiwe@example.test', contact: '0731000006', role: 'customer' },
  { key: 'seed-customer-07', name: 'Refilwe', surname: 'Mahlangu', email: 'refilwe@example.test', contact: '0731000007', role: 'customer' },
  { key: 'seed-customer-08', name: 'Tshepo', surname: 'Radebe', email: 'tshepo@example.test', contact: '0731000008', role: 'customer' },
].map(u => ({ title: '', ...u }));

// ---------- categories (10) ----------
const categories = [
  { id: 'braids', label: 'Braids', desc: 'Box braids, knotless and more', icon: 'braid' },
  { id: 'cornrows', label: 'Cornrows', desc: 'Straight-backs and freestyle patterns', icon: 'lines' },
  { id: 'locs', label: 'Locs', desc: 'Starter locs, retwists and styling', icon: 'loop' },
  { id: 'weaves-wigs', label: 'Weaves & Wigs', desc: 'Sew-ins, closures and wig installs', icon: 'wig' },
  { id: 'natural', label: 'Natural Hair', desc: 'Wash, twist-outs and silk press', icon: 'leaf' },
  { id: 'relaxer', label: 'Relaxer', desc: 'Relaxer application and touch-ups', icon: 'drop' },
  { id: 'colour', label: 'Colour', desc: 'Full colour, highlights and toners', icon: 'palette' },
  { id: 'cuts', label: 'Cuts & Fades', desc: 'Cuts, trims, fades and shape-ups', icon: 'scissors' },
  { id: 'kids', label: 'Kids', desc: 'Gentle styles for children under 12', icon: 'star' },
  { id: 'treatments', label: 'Treatments', desc: 'Deep conditioning and scalp care', icon: 'sparkle' },
].map(({ desc, ...c }) => ({ image: '', description: desc, ...c }));

// ---------- services (20, two per category) ----------
const services = [
  ['knotless-braids', 'Knotless Braids', 'braids', 650, 'Medium knotless braids, waist length.', ['knotless', 'protective', 'long'], 0],
  ['box-braids', 'Box Braids', 'braids', 550, 'Classic box braids, shoulder length.', ['box', 'protective'], 10],
  ['straight-backs', 'Straight-Back Cornrows', 'cornrows', 250, 'Neat straight-back cornrows.', ['straight', 'quick'], 0],
  ['freestyle-cornrows', 'Freestyle Cornrows', 'cornrows', 350, 'Custom cornrow pattern of your choice.', ['design', 'pattern'], 0],
  ['starter-locs', 'Starter Locs', 'locs', 800, 'Comb-coil or two-strand starter locs.', ['starter', 'dreadlocks'], 0],
  ['loc-retwist', 'Loc Retwist', 'locs', 400, 'Retwist and style for existing locs.', ['retwist', 'maintenance'], 5],
  ['sew-in-weave', 'Sew-In Weave', 'weaves-wigs', 700, 'Full sew-in with leave-out.', ['weave', 'sew-in'], 0],
  ['wig-install', 'Frontal Wig Install', 'weaves-wigs', 600, 'Lace frontal install and styling.', ['wig', 'frontal', 'lace'], 15],
  ['wash-and-go', 'Wash & Go', 'natural', 200, 'Cleanse, condition and define curls.', ['curls', 'wash'], 0],
  ['silk-press', 'Silk Press', 'natural', 450, 'Heat-styled silk press on natural hair.', ['straight', 'press'], 0],
  ['relaxer-full', 'Full Relaxer', 'relaxer', 380, 'Full head relaxer with treatment.', ['relaxer', 'straight'], 0],
  ['relaxer-touchup', 'Relaxer Touch-Up', 'relaxer', 280, 'Regrowth touch-up only.', ['relaxer', 'touch-up'], 0],
  ['full-colour', 'Full Colour', 'colour', 750, 'Single-process full head colour.', ['colour', 'dye'], 0],
  ['highlights', 'Highlights', 'colour', 900, 'Foil highlights with toner.', ['highlights', 'blonde'], 10],
  ['ladies-cut', 'Ladies Cut & Style', 'cuts', 300, 'Cut, blow-dry and style.', ['cut', 'trim'], 0],
  ['fade', 'Skin Fade', 'cuts', 150, 'Skin fade with line-up.', ['fade', 'barber'], 0],
  ['kids-cornrows', 'Kids Cornrows', 'kids', 180, 'Simple cornrows for under-12s.', ['kids', 'cornrows'], 0],
  ['kids-cut', 'Kids Cut', 'kids', 100, 'Haircut for under-12s.', ['kids', 'cut'], 0],
  ['deep-condition', 'Deep Conditioning', 'treatments', 250, 'Steam deep-conditioning treatment.', ['moisture', 'treatment'], 0],
  ['scalp-treatment', 'Scalp Treatment', 'treatments', 300, 'Exfoliating scalp detox and massage.', ['scalp', 'treatment'], 20],
].map(([id, title, category_id, price, description, tags, discount]) => ({ id, title, category_id, price, description, tags, image: '', discount }));

// ---------- bookings (15) ----------
function isoDate(offsetDays) {
  const d = new Date();
  d.setDate(d.getDate() + offsetDays);
  const pad = n => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

// [id, customerKey, stylistKey, serviceId, dayOffset, time, type, status, notes]
const bookingRows = [
  ['00000000-0000-4000-8000-000000000001', 'seed-customer-01', 'seed-stylist-thandi', 'knotless-braids', 2, '09:00', 'Incall', 'pending', 'Please use black hair.'],
  ['00000000-0000-4000-8000-000000000002', 'seed-customer-02', 'seed-stylist-lerato', 'highlights', 2, '10:00', 'Incall', 'upcoming', ''],
  ['00000000-0000-4000-8000-000000000003', 'seed-customer-03', 'seed-stylist-sipho', 'fade', 3, '11:00', 'Incall', 'upcoming', ''],
  ['00000000-0000-4000-8000-000000000004', 'seed-customer-04', 'seed-stylist-zara', 'ladies-cut', 3, '14:00', 'Outcall', 'pending', 'Gate code 4512.'],
  ['00000000-0000-4000-8000-000000000005', 'seed-customer-05', 'seed-stylist-sipho', 'loc-retwist', 4, '09:00', 'Incall', 'upcoming', ''],
  ['00000000-0000-4000-8000-000000000006', 'seed-customer-06', 'seed-stylist-thandi', 'box-braids', 5, '08:00', 'Incall', 'pending', ''],
  ['00000000-0000-4000-8000-000000000007', 'seed-customer-07', 'seed-stylist-lerato', 'full-colour', 5, '13:00', 'Incall', 'upcoming', 'Copper shade please.'],
  ['00000000-0000-4000-8000-000000000008', 'seed-customer-08', 'seed-stylist-zara', 'silk-press', 6, '15:00', 'Outcall', 'pending', ''],
  ['00000000-0000-4000-8000-000000000009', 'seed-customer-01', 'seed-stylist-thandi', 'kids-cornrows', 7, '10:00', 'Incall', 'upcoming', 'For my daughter (7).'],
  ['00000000-0000-4000-8000-000000000010', 'seed-customer-02', 'seed-stylist-sipho', 'starter-locs', 8, '12:00', 'Incall', 'pending', ''],
  ['00000000-0000-4000-8000-000000000011', 'seed-customer-03', 'seed-stylist-lerato', 'deep-condition', 9, '16:00', 'Incall', 'upcoming', ''],
  ['00000000-0000-4000-8000-000000000012', 'seed-customer-04', 'seed-stylist-thandi', 'straight-backs', -7, '09:00', 'Incall', 'completed', ''],
  ['00000000-0000-4000-8000-000000000013', 'seed-customer-05', 'seed-stylist-zara', 'wash-and-go', -5, '11:00', 'Incall', 'completed', ''],
  ['00000000-0000-4000-8000-000000000014', 'seed-customer-06', 'seed-stylist-lerato', 'relaxer-touchup', -3, '14:00', 'Incall', 'cancelled', 'Cancelled by customer.'],
  ['00000000-0000-4000-8000-000000000015', 'seed-customer-07', 'seed-stylist-sipho', 'scalp-treatment', 1, '17:00', 'Incall', 'cancelled', 'Declined: stylist unavailable.'],
];

const OUTCALL_ADDRESSES = ['12 Jan Smuts Ave, Parktown', '45 Main Rd, Rondebosch'];

// Builds rows for the bookings table. idByKey maps user keys (e.g. 'seed-customer-01')
// to the real auth user ids created by seed.js.
function buildBookings(idByKey) {
  const byKey = Object.fromEntries(users.map(u => [u.key, u]));
  const byService = Object.fromEntries(services.map(s => [s.id, s]));
  let outcallIndex = 0;

  return bookingRows.map(([id, customerKey, stylistKey, serviceId, offset, time, type, status, notes]) => {
    const customer = byKey[customerKey];
    const service = byService[serviceId];
    const total = Math.round(service.price * (1 - service.discount / 100) * 100) / 100;
    return {
      id,
      customer_id: idByKey[customerKey],
      stylist_id: idByKey[stylistKey],
      service_id: serviceId,
      customer_name: customer.name,
      customer_surname: customer.surname,
      contact: customer.contact,
      type,
      location: type === 'Outcall' ? OUTCALL_ADDRESSES[outcallIndex++ % OUTCALL_ADDRESSES.length] : '',
      booking_date: isoDate(offset),
      booking_time: time,
      notes,
      cost: service.price,
      discount: service.discount,
      total,
      status,
      employee_notified: true,
      customer_notified: true,
    };
  });
}

// ---------- contact-form messages (10, used by the website's contact page) ----------
const messages = [
  ['Ayanda Zulu', 'ayanda@example.test', 'Do you do braids on Sundays?'],
  ['Naledi Sithole', 'naledi@example.test', 'What products do you use for highlights?'],
  ['Kagiso Molefe', 'kagiso@example.test', 'Can I bring my own hair extensions?'],
  ['Precious Ndlovu', 'precious@example.test', 'How far do you travel for outcall bookings?'],
  ['Bongani Mthembu', 'bongani@example.test', 'Is there parking at the salon?'],
  ['Lindiwe Cele', 'lindiwe@example.test', 'Do you offer bridal packages?'],
  ['Refilwe Mahlangu', 'refilwe@example.test', 'How long does a silk press last?'],
  ['Tshepo Radebe', 'tshepo@example.test', 'Do you take card payments?'],
  ['Zinhle Mabaso', 'zinhle@example.test', 'Are you hiring stylists?'],
  ['Musa Ngcobo', 'musa@example.test', 'Can I book for a group of four?'],
].map(([name, email, message], i) => ({ id: `00000000-0000-4000-9000-0000000000${String(i + 1).padStart(2, '0')}`, name, email, message }));

module.exports = { DEMO_PASSWORD, users, categories, services, buildBookings, messages };
