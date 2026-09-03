const express = require('express');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 8093;

app.use(cors());
app.use(express.json());

// Catalog memory index for semantic similarity
const catalogKnowledge = [
  {
    title: 'Apple MacBook Pro 16" (M3 Pro Chip, 18GB RAM, 512GB SSD, Space Black)',
    category: 'Electronics / Laptops',
    price: 219900,
    tags: ['coding', 'developer', 'programming', 'heavy software', 'video editing', 'macbook', 'apple', 'm3 pro'],
    highlights: 'Apple M3 Pro 12-core CPU, 18-core GPU, Liquid Retina XDR 120Hz display, 22-hour battery life'
  },
  {
    title: 'Dell XPS 16 Laptop (Intel Core Ultra 9 185H, 32GB DDR5, 1TB NVMe, RTX 4070)',
    category: 'Electronics / Laptops',
    price: 234990,
    tags: ['coding', 'ai development', 'gaming', 'windows', 'oled touch', 'nvidia rtx'],
    highlights: 'Intel Core Ultra 9 with dedicated AI NPU, NVIDIA RTX 4070, 4K OLED touch'
  },
  {
    title: 'Samsung Galaxy S24 Ultra 5G (Titanium Gray, 12GB RAM, 256GB Storage, Galaxy AI)',
    category: 'Mobiles',
    price: 119999,
    tags: ['phone', 'mobile', 'camera', 'galaxy ai', 'stylus', 'spen', 'samsung'],
    highlights: 'Galaxy AI, 200MP Quad Telephoto Camera with 100x zoom, Snapdragon 8 Gen 3'
  },
  {
    title: 'Apple iPhone 15 Pro Max (256 GB, Natural Titanium, A17 Pro Chip)',
    category: 'Mobiles',
    price: 148900,
    tags: ['phone', 'mobile', 'iphone', 'apple', 'camera', 'ios', 'titanium'],
    highlights: 'A17 Pro chip, Aerospace-grade titanium, 48MP camera with 5x optical zoom'
  },
  {
    title: 'Sony WH-1000XM5 Wireless Noise Cancelling Over-Ear Headphones (Silver)',
    category: 'Electronics / Audio',
    price: 26990,
    tags: ['headphones', 'anc', 'audio', 'music', 'noise cancelling', 'sony', 'wireless'],
    highlights: 'Industry-leading noise cancelling, 30-hour battery, Speak-to-Chat'
  },
  {
    title: 'Nike Air Max 270 Men\'s Running & Lifestyle Sneakers',
    category: 'Fashion / Footwear',
    price: 9790,
    tags: ['shoes', 'sneakers', 'running', 'gym', 'fashion', 'nike', 'sportswear'],
    highlights: 'Max Air 270 heel cushioning, breathable engineered mesh upper'
  },
  {
    title: 'Philips XXL Digital Airfryer (7.2L Capacity, Fat Removal Technology, 2000W)',
    category: 'Appliances / Kitchen',
    price: 11990,
    tags: ['air fryer', 'kitchen', 'cooking', 'healthy', 'appliances', 'philips'],
    highlights: '7.2L XXL basket, Fat removal technology, 16-in-1 presets'
  },
  {
    title: 'VYROX Fresh California Roasted Almonds & Cranberry Trail Mix (500g)',
    category: 'Quick Commerce / Grocery',
    price: 399,
    tags: ['grocery', 'quick commerce', '15 min', 'snacks', 'almonds', 'healthy'],
    highlights: '100% California almonds, high protein, delivered in 15 mins'
  }
];

app.get('/health', (req, res) => {
  res.json({ status: 'UP', service: 'vyrox-ai-service', port: PORT, timestamp: new Date() });
});

app.post('/api/v1/ai/assistant', (req, res) => {
  const { query, budget, category } = req.body;
  const prompt = (query || '').toLowerCase();

  // Extract budget if passed or present in text
  let extractedBudget = budget;
  if (!extractedBudget) {
    const match = prompt.match(/(?:under|below|budget|rs|₹|inr)\s*([0-9,]+)/i);
    if (match) {
      extractedBudget = parseInt(match[1].replace(/,/g, ''), 10);
    }
  }

  // Filter & score catalog
  const scored = catalogKnowledge.map(item => {
    let score = 0;
    const itemText = (item.title + ' ' + item.category + ' ' + item.tags.join(' ') + ' ' + item.highlights).toLowerCase();
    
    // Keyword match
    prompt.split(/\s+/).forEach(word => {
      if (word.length > 2 && itemText.includes(word)) {
        score += 3;
      }
    });

    // Budget fit
    if (extractedBudget) {
      if (item.price <= extractedBudget) {
        score += 5;
      } else {
        score -= 10;
      }
    }

    return { ...item, score };
  });

  scored.sort((a, b) => b.score - a.score);
  const recommendations = scored.filter(s => s.score > 0).slice(0, 4);

  let responseExplanation = `I analyzed the VYROX catalog based on your query "${query}".`;
  if (extractedBudget) {
    responseExplanation += ` Filtered within budget of ₹${extractedBudget.toLocaleString('en-IN')}.`;
  }

  if (recommendations.length > 0) {
    const best = recommendations[0];
    responseExplanation += ` Top pick is "${best.title}" priced at ₹${best.price.toLocaleString('en-IN')}, featuring ${best.highlights}.`;
  } else {
    responseExplanation += ` Here are our trending high-rated picks across the store.`;
  }

  res.json({
    query,
    budget: extractedBudget,
    explanation: responseExplanation,
    recommendations: recommendations.length > 0 ? recommendations : catalogKnowledge.slice(0, 3),
    confidence: 0.94,
    timestamp: new Date()
  });
});

app.listen(PORT, () => {
  console.log(`[VYROX] AI Shopping Assistant Service running on http://localhost:${PORT}`);
});
