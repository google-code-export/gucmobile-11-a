from django.conf.urls.defaults import patterns, include, url

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('backend.views',
     url(r'^getdirections/(?P<origin>[\w\-]+)/(?P<destination>[\w\-]+)/(?P<sensor>[a-z]+)/(?P<alternatives>[a-z]+)/$', 'getdirections'),
     url(r'^admin/', include(admin.site.urls)),
)
