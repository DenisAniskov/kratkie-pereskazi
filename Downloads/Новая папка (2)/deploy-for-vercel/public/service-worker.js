const CACHE_NAME = 'book-summaries-v1';
const urlsToCache = [
  'index.html',
  'manifest.json',
  'icon-192.png',
  'icon-512.png',
  'sudba-cheloveka.html',
  'voina-i-mir.html',
  'revizor.html',
  'bednaya-liza.html',
  'geroy-nashego-vremeni.html',
  'evgeniy-onegin.html',
  'fontawesome/css/all.min.css'
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
  );
});

self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => response || fetch(event.request))
  );
}); 