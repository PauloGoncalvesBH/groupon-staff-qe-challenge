# frozen_string_literal: true

require 'spec_helper'
require 'json'

RSpec.describe 'Health endpoint' do
  it 'returns ok' do
    get '/health'
    expect(last_response.status).to eq 200
    body = JSON.parse(last_response.body)
    expect(body['status']).to eq 'ok'
    expect(body).to have_key 'time'
  end
end
