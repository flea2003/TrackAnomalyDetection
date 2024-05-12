import L from "leaflet";
import { calculateAnomalyColor } from "../../utils/AnomalyColorCalculator";


/**
 * This function creates the ship icon using some magic and HTML canvas
 * @param weight - the color which the ship is going to take, where 0 is green and 1 red
 * @param rotation - how many degrees the ship must be rotated(heading value)
 * @return - the icon of the ship
 */
function createShipIcon(weight: number, rotation: number): L.Icon {
    const color = calculateAnomalyColor(weight * 100);

    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');

    if (!context) {
        throw new Error("Could not create 2D context.");
    }

    let size = 20;
    let padding = 3;

    canvas.width = size;
    canvas.height = size;

    context.translate(10, 10);
    context.rotate(rotation * Math.PI / 180);
    context.translate(-10, -10);
    const path = new Path2D();
    path.moveTo(size / 2, 0);
    path.lineTo(size / 6, size);
    path.lineTo(size / 2, 4 * size / 5);
    path.lineTo(5 * size / 6, size);
    path.lineTo(size / 2, 0);

    context.fillStyle = color;
    context.fill(path);

    context.lineWidth = 0.5;
    context.strokeStyle = "black";
    context.stroke(path);

    return L.icon({
        iconUrl: canvas.toDataURL(),
        iconSize: [canvas.width, canvas.height], // Use canvas size as icon size
        iconAnchor: [canvas.width / 2, canvas.height / 2] // Anchor at the center
    });
}

export default createShipIcon;
