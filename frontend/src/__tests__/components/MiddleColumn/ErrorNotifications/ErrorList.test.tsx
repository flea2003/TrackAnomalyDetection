import "@testing-library/jest-dom";
import { act, fireEvent, render, screen } from "@testing-library/react";
import App, { CurrentPage } from "../../../../App";
import React from "react";
import ErrorNotificationService from "../../../../services/ErrorNotificationService";
import ErrorList from "../../../../components/Side/MiddleColumn/ErrorNotifications/ErrorList";
import useWebSocketClient from "../../../../utils/communication/WebSocketClient";
import ShipDetails from "../../../../model/ShipDetails";

jest.mock("../../../../utils/communication/WebSocketClient", () => ({
  __esModule: true,
  default: jest.fn(),
}));

const mockedUseWebSocketClient = useWebSocketClient as jest.MockedFunction<
  typeof useWebSocketClient
>;

beforeEach(() => {
  mockedUseWebSocketClient.mockReturnValue(new Map<number, ShipDetails>());
});

afterEach(() => {
  ErrorNotificationService.clearAllNotifications();
});

test("When bug icon is clicked, the error list is shown with the current errors", async () => {
  render(<App />);

  act(() => {
    ErrorNotificationService.addError("error text");
  });

  const bugIcon = screen.getByTestId("sidebar-bug-icon");
  fireEvent.click(bugIcon);

  const errorListContainer = screen.getByTestId("error-list-container");
  const errorListEntries = await screen.findAllByTestId("error-list-entry");

  expect(errorListContainer).toBeVisible();
  expect(errorListEntries).toHaveLength(1);
  expect(errorListContainer).toContainElement(errorListEntries[0]);
  expect(errorListEntries[0]).toHaveTextContent("error text");
});

test("Error list shows notifications in reversed order", async () => {
  ErrorNotificationService.addError("error text");
  ErrorNotificationService.addWarning("warning text");
  ErrorNotificationService.addInformation("info text");

  // eslint-disable-next-line @typescript-eslint/no-empty-function
  render(<ErrorList pageChanger={() => {}} />);

  const errorListContainer = screen.getByTestId("error-list-container");
  const errorListEntries = await screen.findAllByTestId("error-list-entry");

  expect(errorListContainer).toBeVisible();
  expect(errorListEntries).toHaveLength(3);
  expect(errorListEntries[0]).toHaveTextContent("info text");
  expect(errorListEntries[1]).toHaveTextContent("warning text");
  expect(errorListEntries[2]).toHaveTextContent("error text");
});

test("Read notifications are distinguished from non-read by class name", async () => {
  ErrorNotificationService.addError("error text");
  ErrorNotificationService.addWarning("warning text");

  // read the second notification
  const notifications = ErrorNotificationService.getAllNotifications();
  notifications[1].wasRead = true;

  // eslint-disable-next-line @typescript-eslint/no-empty-function
  render(<ErrorList pageChanger={() => {}} />);

  const readListEntries = await screen.findAllByTestId("error-list-entry-read");
  const notReadListEntries = await screen.findAllByTestId(
    "error-list-entry-not-read",
  );

  expect(readListEntries).toHaveLength(1);
  expect(readListEntries[0]).toHaveTextContent("warning text");

  expect(notReadListEntries).toHaveLength(1);
  expect(notReadListEntries[0]).toHaveTextContent("error text");
});

test("mark all button test", () => {
  ErrorNotificationService.addError("error text");
  ErrorNotificationService.addWarning("warning text");

  // eslint-disable-next-line @typescript-eslint/no-empty-function
  render(<ErrorList pageChanger={() => {}} />);

  const markAllButton = screen.getByTestId("error-list-mark-all-button");
  fireEvent.click(markAllButton);

  const notifications = ErrorNotificationService.getAllNotifications();
  expect(notifications[0].wasRead).toBe(true);
  expect(notifications[1].wasRead).toBe(true);
});

test("remove notification button test", () => {
  ErrorNotificationService.addError("error text");

  // eslint-disable-next-line @typescript-eslint/no-empty-function
  render(<ErrorList pageChanger={() => {}} />);

  const removeNotificationButton = screen.getByTestId(
    "error-list-entry-trash-icon",
  );
  fireEvent.click(removeNotificationButton);

  const notifications = ErrorNotificationService.getAllNotifications();
  expect(notifications).toHaveLength(0);
});

test("Closing Error list calls pageChanger correctly", () => {
  let wasPageChangerCalled = false;
  const pageChanger = (curPage: CurrentPage) => {
    expect(curPage).not.toBeNull();
    expect(curPage.currentPage).toBe("none");
    expect(curPage.shownItemId).toBe(-1);
    wasPageChangerCalled = true;
  };

  render(<ErrorList pageChanger={pageChanger} />);

  const removeNotificationButton = screen.getByTestId("error-list-close-icon");
  fireEvent.click(removeNotificationButton);

  expect(wasPageChangerCalled).toBe(true);
});
