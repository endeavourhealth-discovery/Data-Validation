import { AngularPage } from './app.po';
import {$, browser, by, element} from 'protractor';
import {StopPage} from './stop.po';

describe('Logon with no permissions', () => {
  let page: AngularPage;

  beforeEach(() => {
    page = new AngularPage();
  });

  it ('Initialize', () => {
    page.navigateTo();

    // Wait for login.
    browser.wait(browser.ExpectedConditions.urlContains('/auth/realms/endeavour/protocol/openid-connect'));
  });

  it ('Perform no-access login', () => {
    element(by.name('username')).sendKeys('e2etest');
    element(by.name('password')).sendKeys('e2eTestPass');
    element(by.name('login')).click();

    // Wait for main app page
    browser.wait(() => $('#content').isPresent());
  });

  it ('Check app loaded', () => {
    expect(page.getTitleText()).toEqual('Data Validation');
  });

  it ('Check permission denied', () => {
    expect(StopPage.isDisplayed()).toEqual(true);
  });

  it ('Logout', () => {
    page.logout();
    browser.wait(browser.ExpectedConditions.urlContains('/auth/realms/endeavour/protocol/openid-connect'));
  });
});