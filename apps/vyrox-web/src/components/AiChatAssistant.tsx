import React, { useState } from 'react';
import { Sparkles, X, Send, Bot, ArrowRight, CornerDownLeft } from 'lucide-react';
import { apiClient } from '../api/apiClient';
import { ProductSummary } from '../types';
import { Link } from 'react-router-dom';

export const AiChatAssistant: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [prompt, setPrompt] = useState('');
  const [loading, setLoading] = useState(false);
  const [messages, setMessages] = useState<Array<{
    sender: 'user' | 'ai';
    text: string;
    products?: ProductSummary[];
  }>>([
    {
      sender: 'ai',
      text: 'Hello! I am VYROX AI, your personal shopping assistant. Ask me anything like "Best laptop for coding under ₹1,50,000" or "Compare top flagship phones"!',
    },
  ]);

  const handleSend = async (queryText?: string) => {
    const textToSend = queryText || prompt;
    if (!textToSend.trim() || loading) return;

    const userMsg = { sender: 'user' as const, text: textToSend };
    setMessages((prev) => [...prev, userMsg]);
    setPrompt('');
    setLoading(true);

    try {
      const res = await apiClient.post('/ai/chat', { prompt: textToSend });
      const aiMsg = {
        sender: 'ai' as const,
        text: res.data.reply,
        products: res.data.recommendedProducts || [],
      };
      setMessages((prev) => [...prev, aiMsg]);
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        { sender: 'ai', text: 'Sorry, I had trouble searching the catalog right now. Please try again.' },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const samplePrompts = [
    'Best laptop for coding under ₹1,50,000',
    'Compare iPhone 15 Pro Max and Galaxy S24 Ultra',
    'Quick snacks delivered in 15 minutes',
    'Best noise cancelling headphones for flights',
  ];

  return (
    <>
      {/* Floating Trigger Button */}
      <button
        onClick={() => setIsOpen(true)}
        className="fixed bottom-6 right-6 z-40 bg-gradient-to-r from-[#0B192C] via-[#1E3E62] to-[#FF6500] text-white p-3.5 sm:px-5 sm:py-3.5 rounded-full shadow-2xl hover:shadow-orange-500/20 hover:scale-105 transition-all flex items-center gap-2 group border border-white/20"
      >
        <Sparkles className="w-5 h-5 text-amber-300 animate-pulse" />
        <span className="hidden sm:inline font-bold text-xs tracking-wide">Ask VYROX AI</span>
      </button>

      {/* Slide-out AI Panel */}
      {isOpen && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex justify-end">
          <div className="bg-white w-full max-w-md h-full shadow-2xl flex flex-col justify-between border-l border-slate-200 animate-in slide-in-from-right duration-300">
            {/* Header */}
            <div className="bg-[#0B192C] text-white p-4 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-[#FF6500] to-amber-400 flex items-center justify-center shadow-md">
                  <Bot className="w-5 h-5 text-white" />
                </div>
                <div>
                  <h3 className="font-bold text-sm leading-tight flex items-center gap-1.5">
                    VYROX AI Assistant
                    <span className="text-[10px] bg-emerald-500/30 text-emerald-300 px-1.5 py-0.5 rounded font-medium">Online</span>
                  </h3>
                  <p className="text-[11px] text-slate-300">Powered by VYROX Catalog Engine</p>
                </div>
              </div>
              <button
                onClick={() => setIsOpen(false)}
                className="p-1.5 hover:bg-white/10 rounded-lg text-slate-300 hover:text-white transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Chat Body */}
            <div className="flex-1 p-4 overflow-y-auto space-y-4 bg-slate-50/60">
              {messages.map((msg, idx) => (
                <div key={idx} className={`flex flex-col ${msg.sender === 'user' ? 'items-end' : 'items-start'}`}>
                  <div
                    className={`max-w-[85%] p-3.5 rounded-2xl text-xs sm:text-sm leading-relaxed ${
                      msg.sender === 'user'
                        ? 'bg-[#0B192C] text-white rounded-br-none shadow-sm'
                        : 'bg-white text-slate-800 border border-slate-200/80 rounded-bl-none shadow-sm'
                    }`}
                  >
                    {msg.text}
                  </div>

                  {/* Render matched product recommendations cards */}
                  {msg.products && msg.products.length > 0 && (
                    <div className="mt-3 w-full space-y-2">
                      <div className="text-[11px] font-bold text-slate-500 uppercase tracking-wider">Top Catalog Matches:</div>
                      {msg.products.slice(0, 3).map((p) => (
                        <Link
                          key={p.id}
                          to={`/product/${p.id}`}
                          onClick={() => setIsOpen(false)}
                          className="flex items-center gap-3 p-2.5 bg-white rounded-xl border border-slate-200 hover:border-[#2B6CB0] shadow-xs hover:shadow-md transition-all group"
                        >
                          <img src={p.mainImageUrl} alt={p.title} className="w-12 h-12 object-contain bg-slate-50 rounded-lg p-1" />
                          <div className="flex-1 min-w-0">
                            <div className="text-xs font-bold text-slate-800 truncate group-hover:text-[#2B6CB0]">{p.title}</div>
                            <div className="text-xs font-black text-slate-900 mt-0.5">₹{p.sellingPrice?.toLocaleString('en-IN')}</div>
                          </div>
                          <ArrowRight className="w-4 h-4 text-slate-400 group-hover:text-[#2B6CB0] flex-shrink-0" />
                        </Link>
                      ))}
                    </div>
                  )}
                </div>
              ))}

              {loading && (
                <div className="flex items-center gap-2 p-3 bg-white rounded-2xl border border-slate-200 max-w-[70%] text-xs text-slate-500">
                  <Sparkles className="w-4 h-4 text-[#FF6500] animate-spin" />
                  <span>Analyzing VYROX catalog...</span>
                </div>
              )}
            </div>

            {/* Quick Suggestions */}
            <div className="p-3 bg-white border-t border-slate-100 flex flex-wrap gap-1.5">
              {samplePrompts.map((sp, idx) => (
                <button
                  key={idx}
                  onClick={() => handleSend(sp)}
                  className="text-[11px] font-medium bg-slate-100 hover:bg-slate-200 text-slate-700 px-2.5 py-1 rounded-full transition-colors truncate max-w-full text-left"
                >
                  💡 {sp}
                </button>
              ))}
            </div>

            {/* Input Form */}
            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleSend();
              }}
              className="p-3 bg-white border-t border-slate-200 flex gap-2"
            >
              <input
                type="text"
                value={prompt}
                onChange={(e) => setPrompt(e.target.value)}
                placeholder="Ask about specs, budget, or deals..."
                className="flex-1 px-3 py-2.5 bg-slate-100 border border-transparent focus:border-[#2B6CB0] focus:bg-white rounded-xl text-xs sm:text-sm outline-none transition-all"
              />
              <button
                type="submit"
                disabled={loading || !prompt.trim()}
                className="p-2.5 bg-[#0B192C] hover:bg-[#1E3E62] disabled:opacity-50 text-white rounded-xl transition-colors shadow-sm"
              >
                <Send className="w-4 h-4" />
              </button>
            </form>
          </div>
        </div>
      )}
    </>
  );
};
