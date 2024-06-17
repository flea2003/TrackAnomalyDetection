import ErrorNotificationService from "../../../services/ErrorNotificationService";
import HttpSender from "../../../utils/communication/HttpSender";

beforeEach(() => {
  // Remove refreshState function, so it does nothing.
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  ErrorNotificationService.refreshState = () => {};
  ErrorNotificationService.clearAllNotifications();
});

afterEach(() => {
  jest.restoreAllMocks();
});

const mockFetch = (ok: boolean, data: string) => {
  jest.spyOn(global, "fetch").mockResolvedValue({
    ok: ok,
    json: jest.fn().mockResolvedValue({ data: data }),
    status: 200,
    statusText: "OK",
    headers: new Headers(),
    redirected: false,
    type: "basic",
    url: "someUrl",
    clone: jest.fn(),
    body: null,
    bodyUsed: false,
    arrayBuffer: jest.fn(),
    blob: jest.fn(),
    formData: jest.fn(),
    text: jest.fn(),
  });
};

test("get good data", async () => {
  mockFetch(true, "testData");
  const result = await HttpSender.get("some-endpoint");
  expect(result).toStrictEqual({ data: "testData" });

  // check that notification was not added
  const errors = ErrorNotificationService.getAllNotifications();
  expect(errors).toHaveLength(0);
});

test("put good data", async () => {
  mockFetch(true, "testData");
  await HttpSender.put("some-endpoint");

  // check that notification was not added
  const errors = ErrorNotificationService.getAllNotifications();
  expect(errors).toHaveLength(0);
});

test("request was not ok", async () => {
  mockFetch(false, "bad data");

  const result = await HttpSender.get("some-endpoint");
  expect(result).toStrictEqual({ data: "bad data" });

  // check that notification was added
  const errors = ErrorNotificationService.getAllNotifications();
  expect(errors).toHaveLength(1);
  expect(errors[0].message).toBe("Error while fetching");
});

test("not ok for put", async () => {
  mockFetch(false, "bad data");

  await HttpSender.put("some-endpoint");

  // check that notification was added
  const errors = ErrorNotificationService.getAllNotifications();
  expect(errors).toHaveLength(1);
  expect(errors[0].message).toBe("Error while fetching some-endpoint");
});

test("fetch throws error", async () => {
  jest.spyOn(global, "fetch").mockImplementation(() => {
    throw new Error("Network error occurred");
  });

  const result = await HttpSender.get("some-endpoint");
  expect(result).toBeNull();

  // check that notification was added
  const errors = ErrorNotificationService.getAllNotifications();
  expect(errors).toHaveLength(1);
  expect(errors[0].message).toBe(
    "Error while fetching some-endpoint: Network error occurred",
  );
});

test("error thrown for put", async () => {
  jest.spyOn(global, "fetch").mockImplementation(() => {
    throw new Error("Network error occurred");
  });

  await HttpSender.put("some-endpoint");

  // check that notification was added
  const errors = ErrorNotificationService.getAllNotifications();
  expect(errors).toHaveLength(1);
  expect(errors[0].message).toBe(
    "Error while fetching some-endpoint: Network error occurred",
  );
});
