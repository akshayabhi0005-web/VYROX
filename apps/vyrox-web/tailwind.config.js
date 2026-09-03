/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        vyrox: {
          navy: '#0B192C',
          royal: '#1E3E62',
          blue: '#2B6CB0',
          cyan: '#00D2FF',
          orange: '#FF6500',
          lightOrange: '#FF884B',
          bg: '#F8F9FD',
          card: '#FFFFFF',
          border: '#E2E8F0',
          textDark: '#1A202C',
          textMuted: '#718096'
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
