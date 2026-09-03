import React, { useState, useEffect } from 'react';
import { Mic, MicOff, X, Sparkles } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface VoiceSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const VoiceSearchModal: React.FC<VoiceSearchModalProps> = ({ isOpen, onClose }) => {
  const [isListening, setIsListening] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [supported, setSupported] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    if (!isOpen) {
      setIsListening(false);
      setTranscript('');
      return;
    }

    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SpeechRecognition) {
      setSupported(false);
      return;
    }

    const recognition = new SpeechRecognition();
    recognition.continuous = false;
    recognition.interimResults = true;
    recognition.lang = 'en-IN';

    recognition.onstart = () => {
      setIsListening(true);
    };

    recognition.onresult = (event: any) => {
      const current = event.resultIndex;
      const text = event.results[current][0].transcript;
      setTranscript(text);
      if (event.results[current].isFinal) {
        setTimeout(() => {
          onClose();
          navigate(`/top-deals?query=${encodeURIComponent(text)}`);
        }, 1200);
      }
    };

    recognition.onerror = (event: any) => {
      console.error('Speech error', event);
      setIsListening(false);
    };

    recognition.onend = () => {
      setIsListening(false);
    };

    recognition.start();

    return () => {
      recognition.stop();
    };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl p-6 sm:p-8 max-w-sm w-full shadow-2xl border border-slate-200 text-center relative animate-in zoom-in-95">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1.5 text-slate-400 hover:text-slate-600 rounded-full hover:bg-slate-100"
        >
          <X className="w-5 h-5" />
        </button>

        <h3 className="font-bold text-lg text-slate-900 mb-1">VYROX Voice Search</h3>
        <p className="text-xs text-slate-500 mb-6">Speak what you are looking for (e.g. "MacBook Pro M3" or "Noise cancelling headphones")</p>

        {supported ? (
          <div className="flex flex-col items-center gap-4">
            <div
              className={`w-20 h-20 rounded-full flex items-center justify-center transition-all ${
                isListening
                  ? 'bg-rose-500 text-white shadow-lg shadow-rose-500/30 animate-pulse scale-110'
                  : 'bg-slate-100 text-slate-400'
              }`}
            >
              <Mic className="w-9 h-9" />
            </div>

            <div className="min-h-[48px] flex items-center justify-center">
              {transcript ? (
                <p className="text-base font-bold text-slate-900 italic">"{transcript}"</p>
              ) : (
                <p className="text-xs text-slate-400">Listening to your voice...</p>
              )}
            </div>
          </div>
        ) : (
          <div className="p-4 bg-amber-50 border border-amber-200 rounded-xl text-xs text-amber-800">
            Web Speech API is not supported in this browser. Please use Chrome/Edge or type in the search bar.
          </div>
        )}
      </div>
    </div>
  );
};
