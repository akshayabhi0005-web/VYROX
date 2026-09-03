// Map Service for OpenStreetMap (OSM) & Nominatim Geocoding
export interface LocationCoordinates {
  lat: number;
  lng: number;
}

export interface GeocodeResult {
  formattedAddress: string;
  streetNumber?: string;
  route?: string;
  locality?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  lat: number;
  lng: number;
}

/**
 * Reverse geocode latitude and longitude using OpenStreetMap Nominatim API
 */
export const reverseGeocode = async (lat: number, lng: number): Promise<GeocodeResult> => {
  try {
    const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`, {
      headers: {
        'Accept-Language': 'en',
      },
    });
    if (!res.ok) {
      throw new Error(`Nominatim error: ${res.statusText}`);
    }
    const data = await res.json();
    return {
      formattedAddress: data.display_name || `${lat.toFixed(4)}, ${lng.toFixed(4)}`,
      city: data.address?.city || data.address?.town || data.address?.state_district || 'Bengaluru',
      locality: data.address?.suburb || data.address?.neighbourhood || data.address?.residential || 'Indiranagar',
      postalCode: data.address?.postcode || '560038',
      state: data.address?.state || 'Karnataka',
      country: data.address?.country || 'India',
      lat,
      lng,
    };
  } catch (err) {
    return {
      formattedAddress: `100 Feet Road, Indiranagar, Bengaluru - 560038`,
      city: 'Bengaluru',
      locality: 'Indiranagar',
      postalCode: '560038',
      state: 'Karnataka',
      country: 'India',
      lat,
      lng,
    };
  }
};

/**
 * Get device current GPS coordinates with high accuracy
 */
export const getCurrentGpsCoordinates = (): Promise<LocationCoordinates> => {
  return new Promise((resolve, reject) => {
    if (!('geolocation' in navigator)) {
      resolve({ lat: 12.9716, lng: 77.5946 });
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        resolve({
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
        });
      },
      (err) => {
        // Fallback default coordinates (Indiranagar, Bengaluru)
        resolve({ lat: 12.9716, lng: 77.5946 });
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 30000,
      }
    );
  });
};
