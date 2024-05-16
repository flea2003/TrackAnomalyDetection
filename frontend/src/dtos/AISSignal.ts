interface AISSignal {
  id: string;
  speed: number;
  long: number;
  lat: number;
  course: number;
  heading: number;
  timestamp: string;
  departurePort: string;
}

export default AISSignal;
