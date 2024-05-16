interface AISSignal {
  id: number;
  speed: number;
  long: number;
  lat: number;
  course: number;
  heading: number;
  timestamp: string;
  departurePort: string;
}

export default AISSignal;
