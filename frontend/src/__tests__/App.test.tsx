import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import App from "../App";
import userEvent from "@testing-library/user-event";

afterAll(() => {
  jest.resetAllMocks();
});

test("By default only the map is loaded when the page opens", async () => {
  render(<App />);

  await waitFor(() => {
    const map = screen.getByTestId("map");
    expect(map).toBeVisible();
  });

  // Make sure the other components are not present
  const notificationsTitle = screen.queryByText("Notifications");
  const anomalyListTitle = screen.queryByText("Anomaly list");

  expect(notificationsTitle).toBeNull();
  expect(anomalyListTitle).toBeNull();
});

test("The map is present when the component loads", () => {
  render(<App />);
  const map = screen.getByTestId("map");
  expect(map).toBeVisible();
});

test("at least 2 icons are present in the sidebar", () => {
  render(<App />);
  const sidebar = screen.getByTestId("sidebar");

  const shipIcon = screen.getByTestId("sidebar-ship-icon");
  const bellIcon = screen.getByTestId("sidebar-bell-icon");

  expect(sidebar).toContainElement(shipIcon);
  expect(sidebar).toContainElement(bellIcon);
});

test("When notifications icon is clicked, notifications appear", async () => {
  render(<App />);
  const notificationsIcon = screen.getByTestId("sidebar-bell-icon");
  await userEvent.click(notificationsIcon);
  await waitFor(() => {
    const notificationsTitle = screen.getByText("Notifications");
    expect(notificationsTitle).toBeVisible();
  });
});

test("When notifications is clicked, only latter is present", async () => {
  render(<App />);
  const notificationsIcon = screen.getByTestId("sidebar-bell-icon");

  await userEvent.click(notificationsIcon);

  await waitFor(() => {
    const notificationsTitle = screen.queryByText("Notifications");
    expect(notificationsTitle).toBeVisible();
  });
});

test("When ships icon is clicked, only latter is present", async () => {
  render(<App />);

  const shipsIcon = screen.getByTestId("sidebar-ship-icon");
  await userEvent.click(shipsIcon);
  const anomalyListElement = screen.getByTestId("anomaly-list-container");

  expect(anomalyListElement).toBeVisible();
});

test("When the close icon is clicked, the anomaly list is not present", async () => {
  render(<App />);

  // Open the list
  const shipIcon = screen.getByTestId("sidebar-ship-icon");
  await userEvent.click(shipIcon);

  // Close the list
  const closeIcon = screen.getByTestId("anomaly-list-close-icon");
  await userEvent.click(closeIcon);

  // Wait for the list to disappear
  await waitFor(() => {
    const anomalyListTitle = screen.queryByText("Anomaly List");
    expect(anomalyListTitle).toBeNull();
  });
});

test("When the user closes the list and opens it again, it is present", async () => {
  render(<App />);

  // Open the list
  const shipIcon1 = screen.getByTestId("sidebar-ship-icon");
  await userEvent.click(shipIcon1);

  // Close the list
  const closeIcon = screen.getByTestId("anomaly-list-close-icon");
  await userEvent.click(closeIcon);

  // Open the list
  const shipIcon2 = screen.getByTestId("sidebar-ship-icon");
  await userEvent.click(shipIcon2);

  await waitFor(() => {
    const anomalyListElement = screen.getByTestId("anomaly-list-container");
    expect(anomalyListElement).toBeVisible();
  });
});

test("Double clicking the ship icon opens and closes the list", async () => {
  render(<App />);

  // Open the list
  const shipIcon = screen.getByTestId("sidebar-ship-icon");
  await userEvent.click(shipIcon);

  // Close the list
  await userEvent.click(shipIcon);

  await waitFor(() => {
    const anomalyListElement = screen.queryByTestId("anomaly-list-container");
    expect(anomalyListElement).toBeNull();
  });
});
