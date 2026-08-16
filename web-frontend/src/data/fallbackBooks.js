export const FALLBACK_BOOKS = [
  {
    id: 1,
    title: "To Kill a Mockingbird",
    author: "Harper Lee",
    genre: "Fiction",
    description: "A compelling story of racial injustice and the destruction of innocence in the American South.",
    coverUrl: "https://covers.openlibrary.org/b/id/8225266-L.jpg",
    publishedYear: 1960,
    rating: 4.8
  },
  {
    id: 2,
    title: "1984",
    author: "George Orwell",
    genre: "Science Fiction",
    description: "A dystopian vision of a totalitarian regime where Big Brother is always watching.",
    coverUrl: "https://covers.openlibrary.org/b/id/8745958-L.jpg",
    publishedYear: 1949,
    rating: 4.8
  },
  {
    id: 3,
    title: "Pride and Prejudice",
    author: "Jane Austen",
    genre: "Romance",
    description: "A classic romance exploring manners, upbringing, and marriage in 19th century England.",
    coverUrl: "https://covers.openlibrary.org/b/id/14348537-L.jpg",
    publishedYear: 1813,
    rating: 4.7
  },
  {
    id: 4,
    title: "The Great Gatsby",
    author: "F. Scott Fitzgerald",
    genre: "Classic Literature",
    description: "A tragic tale of ambition, love, and the American Dream in the Roaring Twenties.",
    coverUrl: "https://covers.openlibrary.org/b/id/7222246-L.jpg",
    publishedYear: 1925,
    rating: 4.5
  },
  {
    id: 5,
    title: "The Catcher in the Rye",
    author: "J.D. Salinger",
    genre: "Fiction",
    description: "A story of teenage rebellion, alienation, and identity in post-WWII America.",
    coverUrl: "https://covers.openlibrary.org/b/id/9273490-L.jpg",
    publishedYear: 1951,
    rating: 4.2
  },
  {
    id: 6,
    title: "Dune",
    author: "Frank Herbert",
    genre: "Science Fiction",
    description: "An epic sci-fi saga of politics, religion, and survival on the desert planet Arrakis.",
    coverUrl: "https://covers.openlibrary.org/b/id/8100921-L.jpg",
    publishedYear: 1965,
    rating: 4.9
  },
  {
    id: 7,
    title: "The Hobbit",
    author: "J.R.R. Tolkien",
    genre: "Fantasy",
    description: "Bilbo Baggins journeys to victory and treasure in Middle-earth.",
    coverUrl: "https://covers.openlibrary.org/b/id/8406786-L.jpg",
    publishedYear: 1937,
    rating: 4.9
  },
  {
    id: 8,
    title: "Harry Potter and the Sorcerer's Stone",
    author: "J.K. Rowling",
    genre: "Fantasy",
    description: "An orphaned boy discovers he is a wizard and attends Hogwarts School of Witchcraft and Wizardry.",
    coverUrl: "https://covers.openlibrary.org/b/id/10521270-L.jpg",
    publishedYear: 1997,
    rating: 4.9
  },
  {
    id: 9,
    title: "Atomic Habits",
    author: "James Clear",
    genre: "Self-Help",
    description: "An easy & proven way to build good habits & break bad ones.",
    coverUrl: "https://covers.openlibrary.org/b/id/12889269-L.jpg",
    publishedYear: 2018,
    rating: 4.8
  },
  {
    id: 10,
    title: "Sapiens: A Brief History of Humankind",
    author: "Yuval Noah Harari",
    genre: "Non-Fiction",
    description: "A groundbreaking narrative of humanity’s creation and evolution.",
    coverUrl: "https://covers.openlibrary.org/b/id/12539702-L.jpg",
    publishedYear: 2011,
    rating: 4.7
  },
  {
    id: 11,
    title: "The Alchemist",
    author: "Paulo Coelho",
    genre: "Fiction",
    description: "An inspiring fable about an Andalusian shepherd boy following his dreams.",
    coverUrl: "https://covers.openlibrary.org/b/id/10006520-L.jpg",
    publishedYear: 1988,
    rating: 4.6
  },
  {
    id: 12,
    title: "Sherlock Holmes: Complete Novels",
    author: "Arthur Conan Doyle",
    genre: "Mystery",
    description: "The iconic detective solves baffling mysteries across Victorian London.",
    coverUrl: "https://covers.openlibrary.org/b/id/7984916-L.jpg",
    publishedYear: 1887,
    rating: 4.8
  }
];

export function getFallbackBooks(genre, search) {
  let filtered = [...FALLBACK_BOOKS];
  if (genre) {
    filtered = filtered.filter(b => b.genre.toLowerCase() === genre.toLowerCase());
  }
  if (search) {
    const q = search.toLowerCase();
    filtered = filtered.filter(b => 
      b.title.toLowerCase().includes(q) || 
      b.author.toLowerCase().includes(q) ||
      b.genre.toLowerCase().includes(q)
    );
  }
  return filtered;
}
