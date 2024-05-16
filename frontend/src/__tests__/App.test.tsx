import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import App from "../App";
import userEvent from "@testing-library/user-event";

test("By default the anomaly list is loaded by default when page opens", () => {
  render(<App />);
  const anomalyListTitle = screen.getByText("Anomaly list");
  expect(anomalyListTitle).toBeVisible();
});

test("The map is present when the component loads", () => {
  render(<App />);
  const map = screen.getByTestId("map");
  expect(map).toBeVisible();
});

test("3 icons are present in the sidebar", () => {
  render(<App />);
  const sidebar = screen.getByTestId("sidebar");

  const shipIcon = screen.getByTestId("sidebar-ship-icon");
  const bellIcon = screen.getByTestId("sidebar-bell-icon");
  const settingsIcon = screen.getByTestId("sidebar-settings-icon");

  expect(sidebar).toContainElement(shipIcon);
  expect(sidebar).toContainElement(bellIcon);
  expect(sidebar).toContainElement(settingsIcon);
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

test("When settings icon is clicked, settings appear", async () => {
  render(<App />);
  const settingsIcon = screen.getByTestId("sidebar-settings-icon");
  await userEvent.click(settingsIcon);
  await waitFor(() => {
    const settingsTitle = screen.getByText("Settings");
    expect(settingsTitle).toBeVisible();
  });
});

test("When settings is clicked and then notifications is clicked, only latter is present", async () => {
  render(<App />);
  const settingsIcon = screen.getByTestId("sidebar-settings-icon");
  const notificationsIcon = screen.getByTestId("sidebar-bell-icon");

  await userEvent.click(settingsIcon);
  await userEvent.click(notificationsIcon);

  await waitFor(() => {
    const settingsTitle = screen.queryByText("Settings");
    expect(settingsTitle).toBeNull();
  });

  await waitFor(() => {
    const notificationsTitle = screen.queryByText("Notifications");
    expect(notificationsTitle).toBeVisible();
  });
});

test("When settings is clicked and then ships icon is clicked, only latter is present", async () => {
  render(<App />);
  const settingsIcon = screen.getByTestId("sidebar-settings-icon");
  await userEvent.click(settingsIcon);

  const shipsIcon = screen.getByTestId("sidebar-ship-icon");
  await userEvent.click(shipsIcon);

  const settingsTitle = screen.queryByText("Settings");
  const anomalyListTitle = screen.queryByText("Anomaly list");

  expect(settingsTitle).toBeNull();
  expect(anomalyListTitle).toBeVisible();
});
