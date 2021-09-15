import { check, group, sleep } from 'k6';
import http from 'k6/http';
import { Rate, Trend } from 'k6/metrics';

const postStatusTrend = new Trend('desktopView');
const postStatusErrorRate = new Rate('desktopView errors');

const statusEndpointTrend = new Trend('wallboardWithLocation');
const statusEndpointErrorRate = new Rate('wallboardWithLocation errors');

const getEndpointTrend = new Trend('wallboardWithoutLocation');
const getEndpointErrorRate = new Rate('wallboardWithoutLocation errors');

export const options = {
  thresholds: {
    errors: [{ threshold: 'count<1', abortOnFail: false }],
    checks: [{ threshold: 'rate<0.1', abortOnFail: false }],
  },
  stages: [
    { duration: '1m', target: 100 },
    { duration: '30s', target: 100 },
    // { duration: '2m', target: 100 }, // increase virtual users from 0 to 100 over 2 minutes
    // { duration: '5m', target: 100 }, // hold 100 for 5 minutes
    // { duration: '2m', target: 200 }, // increase virtual users from 100 to 200 over 2 minutes
    // { duration: '5m', target: 200 }, // hold 200 for 5 minutes
    // { duration: '10s', target: 1400 }, // spike to 1400 users
    // { duration: '3m', target: 1400 }, // stay at 1400 for 3 minutes
    // { duration: '5m', target: 300 },
    // { duration: '2m', target: 400 }, // increase virtual users from 300 to 400 over 2 minutes
    // { duration: '5m', target: 400 }, // hold 200 for 5 minutes
    // { duration: '10m', target: 0 },  // decrease virtual users from 400 to 0 over 10 minutes
  ],
};

export function setup() {}

export function teardown(data) {}

const BASE_URL = 'http://httpbin.org';

export default () => {
  const params = {};

  group('example with url batch', () => {
    const results = http.batch([
      { method: 'GET', url: `${BASE_URL}/status/200`, params },
      { method: 'GET', url: `${BASE_URL}/get`, params },
    ]);

    check(results[0], {
      'status endpoint is status 200': (res) => res.status === 200,
    }) || statusEndpointErrorRate.add(1);
    statusEndpointTrend.add(results[0].timings.duration);

    check(results[1], {
      '"/get" endpoint is status 200': (res) => res.status === 200,
    }) || getEndpointErrorRate.add(1);
    getEndpointTrend.add(results[1].timings.duration);

    sleep(1);
  });

  group('single post example', () => {
    const result = http.post(`${BASE_URL}/status/200`, params);

    check(result, {
      'desktop view: is status 200': (res) => res.status === 200,
    }) || postStatusErrorRate.add(1);
    postStatusTrend.add(result.timings.duration);

    sleep(1);
  });
};
