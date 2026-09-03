import React, { useState, useEffect, useRef } from 'react';
import { Mic, MicOff, X, Sparkles, AlertCircle, ArrowRight, RefreshCw } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface VoiceSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const VoiceSearchModal: React.FC<VoiceSearchModalProps> = ({ isOpen, onClose }) => {
  const [isListening, setIsListening] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [supported, setSupported] = useState(true);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const recognitionRef = useRef<any>(null);
  const navigate = useNavigate();

  const startListening = () => {
    setErrorMsg(null);
    setTranscript('');

    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (!SpeechRecognition) {
      setSupported(false);
      return;
    }

    try {
      if (recognitionRef.current) {
        try { recognitionRef.current.stop(); } catch (_) {}
      }

      const recognition = new SpeechRecognition();
      recognition.continuous = false;
      recognition.interimResults = true;
      recognition.lang = 'en-IN';

      recognition.onstart = () => {
        setIsListening(true);
        setErrorMsg(null);
      };

      recognition.onresult = (event: any) => {
        const current = event.resultIndex;
        const text = event.results[current][0].transcript;
        setTranscript(text);
        if (event.results[current].isFinal) {
          setTimeout(() => {
            onClose();
            navigate(`/top-deals?query=${encodeURIComponent(text.trim())}`);
          }, 900);
        }
      };

      recognition.onerror = (event: any) => {
        console.warn('Speech recognition event error:', event.error);
        setIsListening(false);
        if (event.error === 'not-allowed' || event.error === 'permission-denied') {
          setErrorMsg('Microphone access was denied. Please allow microphone permissions in your browser.');
        } else if (event.error === 'no-speech') {
          setErrorMsg('No speech was detected. Tap the mic to try speaking again.');
        } else if (event.error === 'network') {
          setErrorMsg('Network issue during speech recognition. You can type in the search bar.');
        } else {
          setErrorMsg(`Voice input error (${event.error}). Please try again.`);
        }
      };

      recognition.onend = () => {
        setIsListening(false);
      };

      recognitionRef.current = recognition;
      recognition.start();
    } catch (e: any) {
      console.error('Failed to start speech recognition', e);
      setIsListening(false);
      setErrorMsg('Could not initialize speech recognition. Please check microphone settings.');
    }
  };

  useEffect(() => {
    if (!isOpen) {
      setIsListening(false);
      setTranscript('');
      setErrorMsg(null);
      if (recognitionRef.current) {
        try { recognitionRef.current.stop(); } catch (_) {}
      }
      return;
    }

    startListening();

    return () => {
      if (recognitionRef.current) {
        try { recognitionRef.current.stop(); } catch (_) {}
      }
    };
  }, [isOpen]);

  const handleManualSearchSubmit = () => {
    if (transcript.trim()) {
      onClose();
      navigate(`/top-deals?query=${encodeURIComponent(transcript.trim())}`);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl p-6 sm:p-8 max-w-sm w-full shadow-2xl border border-slate-200 text-center relative animate-in zoom-in-95">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1.5 text-slate-400 hover:text-slate-600 rounded-full hover:bg-slate-100 transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="w-10 h-10 rounded-2xl bg-orange-100 text-[#FF6500] flex items-center justify-center mx-auto mb-3">
          <Sparkles className="w-5 h-5" />
        </div>

        <h3 className="font-black text-lg text-slate-900 mb-1">VYROX Voice Search</h3>
        <p className="text-xs text-slate-500 mb-6">
          Speak what you want to buy (e.g. "MacBook Pro M3" or "Samsung Galaxy S24")
        </p>

        {supported ? (
          <div className="flex flex-col items-center gap-5">
            <button
              type="button"
              onClick={isListening ? () => recognitionRef.current?.stop() : startListening}
              className={`w-22 h-22 rounded-full flex items-center justify-center transition-all cursor-pointer relative ${
                isListening
                  ? 'bg-rose-500 text-white shadow-xl shadow-rose-500/40 ring-8 ring-rose-100 animate-pulse scale-105'
                  : 'bg-slate-100 text-slate-500 hover:bg-slate-200 shadow-md'
              }`}
            >
              {isListening ? (
                <Mic className="w-10 h-10 animate-bounce" />
              ) : (
                <Mic className="w-10 h-10" />
              )}
            </button>

            <div className="min-h-[48px] w-full flex items-center justify-center px-2">
              {transcript ? (
                <div className="space-y-2">
                  <p className="text-base font-extrabold text-slate-900 italic">"{transcript}"</p>
                  <button
                    onClick={handleManualSearchSubmit}
                    className="inline-flex items-center gap-1 text-xs font-bold text-[#FF6500] hover:underline"
                  >
                    <span>Search now</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </button>
                </div>
              ) : isListening ? (
                <p className="text-xs font-medium text-slate-600 flex items-center gap-1.5 animate-pulse">
                  <span className="w-2 h-2 rounded-full bg-rose-500"></span>
                  Listening... Speak clearly into your mic
                </p>
              ) : (
                <p className="text-xs text-slate-400">Tap microphone above to speak</p>
              )}
            </div>

            {errorMsg && (
              <div className="p-3 bg-amber-50 border border-amber-200 rounded-2xl text-xs text-amber-800 flex items-start gap-2 text-left w-full">
                <AlertCircle className="w-4 h-4 flex-shrink-0 text-amber-600 mt-0.5" />
                <div className="flex-1">
                  <span>{errorMsg}</span>
                  <button
                    onClick={startListening}
                    className="mt-1.5 flex items-center gap-1 text-[11px] font-bold text-[#0B192C] hover:underline"
                  >
                    <RefreshCw className="w-3 h-3" /> Try Again
                  </button>
                </div>
              </div>
            )}
          </div>
        ) : (
          <div className="p-4 bg-amber-50 border border-amber-200 rounded-2xl text-xs text-amber-800 space-y-2">
            <p className="font-bold">Web Speech API is not supported in this browser.</p>
            <p className="text-[11px]">Please use Google Chrome, Microsoft Edge, or Android Chrome, or use standard text search.</p>
          </div>
        )}
      </div>
    </div>
  );
};
