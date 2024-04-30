import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';

test('renders learn react link', () => {
  render(<App />);
  const linkElement = screen.getByText(/Do not press this button/i);
  expect(linkElement).toBeInTheDocument();
});

test('example test', () => {
  expect(2 + 3).toBe(5);
});