import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { act } from '@testing-library/react';
// import '@testing-library/jest-dom/extend-expect';
import '@testing-library/jest-dom'
import App from '../App';
import exp from 'constants';
import userEvent from '@testing-library/user-event';

test('By default the anomaly list is loaded by default when page opens', () => {
  render(<App />);
  const anomalyListTitle = screen.getByText('Anomaly list');
  expect(anomalyListTitle).toBeVisible();
});

test('The map is present when the component loads', () => {
  render(<App />);
  const map = screen.getByTestId('map');
  expect(map).toBeVisible();
});

test('3 icons are present in the sidebar', () => {
  render(<App />);
  const sidebar = screen.getByTestId('sidebar');
  expect(sidebar.children.length).toBe(3);
});

test('When notifications icon is clicked, notifications appear', async () => {
  render(<App />);
  const notificationsIcon = screen.getByTestId('sidebar-bell-icon');
  userEvent.click(notificationsIcon);
  await waitFor(() => {
    const notificationsTitle = screen.getByText('Notifications');
    expect(notificationsTitle).toBeVisible();
  });
});

test('When settings icon is clicked, settings appear', async () => {
  render(<App />);
  const settingsIcon = screen.getByTestId('sidebar-settings-icon');
  userEvent.click(settingsIcon);
  await waitFor(() => {
    const settingsTitle = screen.getByText('Settings');
    expect(settingsTitle).toBeVisible();
  });
});

test('When settings is clicked and then notifications is clicked, only latter is present', async () => {
  render(<App />);
  const settingsIcon = screen.getByTestId('sidebar-settings-icon');
  const notificationsIcon = screen.getByTestId('sidebar-bell-icon');

  userEvent.click(settingsIcon);
  userEvent.click(notificationsIcon);

  await waitFor(() => {
    const settingsTitle = screen.queryByText('Settings');
    const notificationsTitle = screen.queryByText('Notifications');
    expect(settingsTitle).toBeNull();
    expect(notificationsTitle).toBeVisible();
  });
});

test('When settings is clicked and then ships icon is clicked, only latter is present', async () => {
  render(<App />);
  const settingsIcon = screen.getByTestId('sidebar-settings-icon');
  userEvent.click(settingsIcon);

  const shipsIcon = screen.getByTestId('sidebar-ship-icon');
  userEvent.click(shipsIcon);

  const settingsTitle = screen.queryByText('Settings');
  const anomalyListTitle = screen.queryByText('Anomaly list');

  expect(settingsTitle).toBeNull();
  expect(anomalyListTitle).toBeVisible();
});