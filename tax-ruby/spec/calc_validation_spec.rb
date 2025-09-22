# frozen_string_literal: true

require 'spec_helper'
require 'json'

RSpec.describe 'POST /tax/calc - validations' do
  def post_json(body)
    post '/tax/calc', body.to_json, { 'CONTENT_TYPE' => 'application/json' }
  end

  it 'returns 400 when amount is missing' do
    post_json(currency: 'USD')
    expect(last_response.status).to eq 400
    body = JSON.parse(last_response.body)
    expect(body['error']).to match(/amount is required/)
  end

  it 'returns 400 when currency is missing' do
    post_json(amount: 10.0)
    expect(last_response.status).to eq 400
    body = JSON.parse(last_response.body)
    expect(body['error']).to match(/currency is required/)
  end

  it 'returns 400 for invalid JSON' do
    post '/tax/calc', '{ bad json', { 'CONTENT_TYPE' => 'application/json' }
    expect(last_response.status).to eq 400
    body = JSON.parse(last_response.body)
    expect(body['error']).to match(/invalid JSON/)
  end

  it 'returns 400 when amount is not a number' do
    post_json(amount: '100.0', currency: 'USD')
    expect(last_response.status).to eq 400
    body = JSON.parse(last_response.body)
    expect(body['error']).to match(/amount must be a number/)
  end

  it 'returns 400 when currency is not a 3-letter string' do
    post_json(amount: 10.0, currency: 'US')
    expect(last_response.status).to eq 400
    body = JSON.parse(last_response.body)
    expect(body['error']).to match(/currency must be a 3-letter string/)
  end
end
