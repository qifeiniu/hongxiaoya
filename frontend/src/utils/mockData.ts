const femaleAvatars = [
  '/static/people/female/aiony-haust-3TLl_97HNJo-unsplash.jpg',
  '/static/people/female/ayo-ogunseinde-6W4F62sN_yI-unsplash.jpg',
  '/static/people/female/christina-wocintechchat-com-m-0Zx1bDv5BNY-unsplash.jpg',
  '/static/people/female/gabriel-silverio-K_b41GaWC5Y-unsplash.jpg',
  '/static/people/female/stefan-stefancik-QXevDflbl8A-unsplash.jpg'
];

const maleAvatars = [
  '/static/people/male/ali-morshedlou-WMD64tMfc4k-unsplash.jpg',
  '/static/people/male/charlie-green-3JmfENcL24M-unsplash.jpg',
  '/static/people/male/ian-dooley-d1UPkiFd04A-unsplash.jpg',
  '/static/people/male/janko-ferlic-G-jo31ESuRE-unsplash.jpg',
  '/static/people/male/nate-J5U-22o1ubw-unsplash.jpg',
  '/static/people/male/vicky-hladynets-C8Ta0gwPbQg-unsplash.jpg'
];

export const DEFAULT_AVATAR = '/static/avatars/default-avatar.png';

export const getRandomAvatar = (userId?: number, gender: 'male' | 'female' = 'female') => {
  if (userId === undefined || userId === null || userId === 0) return DEFAULT_AVATAR;
  const list = gender === 'male' ? maleAvatars : femaleAvatars;
  // Use user ID to get a deterministic but random-looking photo
  const id = userId ? (userId % list.length) : Math.floor(Math.random() * list.length);
  return list[id];
};

const lifestylePhotos = [
  ...femaleAvatars,
  ...maleAvatars
];

export const getMockPhotos = (count: number = 3) => {
  const photos = [];
  for (let i = 0; i < count; i++) {
    const id = Math.floor(Math.random() * lifestylePhotos.length);
    photos.push(lifestylePhotos[id]);
  }
  return photos;
};

export const getMockPhotosByGender = (userId?: number, gender: 'male' | 'female' = 'female', count: number = 3) => {
  const list = gender === 'male' ? maleAvatars : femaleAvatars;
  const start = userId ? userId % list.length : Math.floor(Math.random() * list.length);
  const photos = [];

  for (let i = 0; i < count; i++) {
    photos.push(list[(start + i) % list.length]);
  }

  return photos;
};
