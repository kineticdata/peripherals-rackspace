require File.expand_path(File.join(File.dirname(__FILE__), 'dependencies'))

class RackspaceServerCreateV1
  def initialize(input)
    # Set the input document attribute
    @input_document = REXML::Document.new(input)
    
    # Store the info values in a Hash of info names to values.
    @info_values = {}
    REXML::XPath.each(@input_document,"/handler/infos/info") { |item|
      @info_values[item.attributes['name']] = item.text
    }
    
    # Retrieve all of the handler parameters and store them in a hash attribute
    # named @parameters.
    @parameters = {}
    REXML::XPath.match(@input_document, 'handler/parameters/parameter').each do |node|
      @parameters[node.attribute('name').value] = node.text.to_s
    end

    @enable_debug_logging = @info_values['enable_debug_logging'] == 'Yes'
  end
  
  def execute()
    puts "Initializing fog the service" if @enable_debug_logging
    service_options = {
      :provider => 'rackspace',
      :rackspace_username => @info_values["username"],
      :rackspace_api_key => @info_values["api_key"],
      :version => :v2
    }
    if @parameters['rackspace_region'] != ""
      service_options[:rackspace_region] = @parameters['rackspace_region'].to_sym
    end
    service = Fog::Compute.new(service_options)

    flavor = service.flavors.find {|i| i.name =~ Regexp.new(@parameters['flavor_name'])}
    image = service.images.find {|i| i.name =~ Regexp.new(@parameters['image_name'])}

    if flavor.nil?
      raise StandardError, "Invalid Flavor Name: Cannot find a flavor with the name of #{@parameters['flavor_name']}"
    elsif image.nil?
      raise StandardError, "Invalid Image Name: Cannot find an image with the name of #{@parameters['image_name']}"
    end

    begin
      server = service.servers.create({:name => @parameters['server_name'], 
        :flavor_id => flavor.id, :image_id => image.id})
    rescue Fog::Compute::RackspaceV2::ServiceError => error
      raise StandardError, error.message
    end

    puts "Returning the results" if @enable_debug_logging
    <<-RESULTS
    <results>
      <result name="server_id">#{server.id}</result>
      <result name="server_name">#{server.name}</result>
      <result name="image_id">#{image.id}</result>
      <result name="image_name">#{image.name}</result>
      <result name="flavor_id">#{flavor.id}</result>
      <result name="flavor_name">#{flavor.name}</result>
      <result name="flavor_ram">#{flavor.ram}</result>
      <result name="flavor_system_disk">#{flavor.disk}</result>
      <result name="flavor_cpus">#{flavor.vcpus}</result>
    </results>
    RESULTS
  end

  # This is a template method that is used to escape results values (returned in
  # execute) that would cause the XML to be invalid.  This method is not
  # necessary if values do not contain character that have special meaning in
  # XML (&, ", <, and >), however it is a good practice to use it for all return
  # variable results in case the value could include one of those characters in
  # the future.  This method can be copied and reused between handlers.
  def escape(string)
    # Globally replace characters based on the ESCAPE_CHARACTERS constant
    string.to_s.gsub(/[&"><]/) { |special| ESCAPE_CHARACTERS[special] } if string
  end
  # This is a ruby constant that is used by the escape method
  ESCAPE_CHARACTERS = {'&'=>'&amp;', '>'=>'&gt;', '<'=>'&lt;', '"' => '&quot;'}
end
