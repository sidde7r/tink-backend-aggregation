const request = require('request-promise');
const Promise = require('bluebird');
const inquirer = require('inquirer');

const BASE_URL = 'https://www.tink.se/api/v1';

async function execute() {
  const DEMO_IDs = [
    '201212121212',
    '201212121213',
    '201212121214',
    '201212121215',
    '201212121216',
    '201212121217'
  ]
  let confirm = { confirm: false };
  let params = null;
  while (!confirm.confirm) {
    params = await inquirer.prompt([
      {
        name: 'username',
        message: 'Username'
      },
      {
        name: 'password',
        message: 'Password'
      },
      {
        name: 'market',
        default: 'GB',
        message: 'Market'
      },
      {
        name: 'demoCredentials',
        type: 'list',
        default: '201212121217',
        message: 'Credentials',
        choices: DEMO_IDs
      }
    ]);
    console.log(`Username: ${params.username}\nPassword: ${params.password}\nMarket: ${params.market}\nDemo credentials:${params.demoCredentials}`)
    confirm = await inquirer.prompt([
      {
        type: 'confirm',
        name: 'confirm',
        message: 'Is this correct?',
      }
    ]);
  }
  console.log('Registering user...');
  await request({
    method: 'POST',
    uri: `${BASE_URL}/user/register`,
    headers: {
      'X-Tink-Device-ID': 'b24000e6723643599aea11b6293d3943',
      'X-Tink-Client-Key': '55a7aa84a69843e583cf7cc994b7ad9f',
    },
    json: true,
    body: {
      profile: { market: params.market },
      username: params.username,
      password: params.password
    }
  });
  console.log('Adding credentials...')
  await request({
    method: 'POST',
    uri: `${BASE_URL}/credentials/`,
    headers: {
      'X-Tink-Device-ID': 'b24000e6723643599aea11b6293d3943',
      'X-Tink-Client-Key': '55a7aa84a69843e583cf7cc994b7ad9f',
    },
    auth: {
      user: params.username,
      pass: params.password
    },
    json: true,
    body: {
      providerName: params.market.toLowerCase() + '-demo',
      fields: { username: params.demoCredentials, password: '1212' }
    }
  });
  await request({
    method: 'PUT',
    uri: `${BASE_URL}/user/profile`,
    headers: {
      'X-Tink-Device-ID': 'b24000e6723643599aea11b6293d3943',
      'X-Tink-Client-Key': '55a7aa84a69843e583cf7cc994b7ad9f',
    },
    auth: {
      user: params.username,
      pass: params.password
    },
    json: true,
    body: {
      'birth': null,
      'currency': 'GBP',
      'gender': null,
      'locale': 'en_US',
      'market': params.market,
      'periodAdjustedDay': 25,
      'periodMode': 'MONTHLY_ADJUSTED',
      'timeZone': 'Europe/Stockholm',
      'fraudPersonNumber': null,
      'name': null,
      'notificationSettings': {
        'balance': false,
        'budget': false,
        'doubleCharge': false,
        'income': false,
        'largeExpense': false,
        'summaryMonthly': false,
        'summaryWeekly': false,
        'transaction': false,
        'unusualAccount': false,
        'unusualCategory': false,
        'einvoices': false,
        'fraud': false
      }
    }
  });
}

execute().catch((e) => {
  console.error(e);
  process.exit(1);
});
