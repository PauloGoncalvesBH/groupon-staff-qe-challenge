# frozen_string_literal: true

ENV['RACK_ENV'] = 'test'

require 'rack/test'
require 'rspec'
require_relative '../app'

RSpec.configure do |config|
  config.include Rack::Test::Methods
  config.order = :random
  config.example_status_persistence_file_path = ".rspec_status"
  Kernel.srand config.seed
end

def app
  Sinatra::Application
end
