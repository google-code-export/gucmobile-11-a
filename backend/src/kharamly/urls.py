from django.conf.urls.defaults import patterns, include, url

# Uncomment the next two lines to enable the admin:
from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('backend.views',
    url(r'^api/(?P<from>(.)+)/(?P<to>(.)+)/(?P<who>[\w\-_]+)$', 'api'),
    url(r'^test/(?P<test_value>(.)+)', 'test_method_in_views'),
    url(r'^test_evaluate/', 'route_blockage'),
    url(r'^directions/(?P<origin>(.)+)/(?P<destination>(.)+)/([a-z]+)/$', 'directions'),
    url(r'^alternatives/(?P<location>\d+)/(?P<destination>\d+)$', 'alternatives'),
    url(r'^admin/', include(admin.site.urls)),
)
