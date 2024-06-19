import ErrorNotificationService from "../../services/ErrorNotificationService";

test("Warning should be printed if the refresh state is not set up", () => {
  // mock the `console.log` function
  console.log = jest.fn();
  ErrorNotificationService.refreshState();

  expect(console.log).toHaveBeenCalledWith(
    "ErrorNotificationService refreshState was not set up",
  );
});
